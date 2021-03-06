package peergos.shared.user.fs;

import peergos.shared.crypto.*;
import peergos.shared.crypto.random.*;
import peergos.shared.crypto.symmetric.*;
import peergos.shared.user.*;
import peergos.shared.util.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class DirAccess extends FileAccess {

    public static final int MAX_CHILD_LINKS_PER_BLOB = 500;

    private final SymmetricLink subfolders2files, subfolders2parent;
    private List<SymmetricLocationLink> subfolders, files;
    private final Optional<SymmetricLocationLink> moreFolderContents;

    public DirAccess(SymmetricLink subfolders2files, SymmetricLink subfolders2parent, List<SymmetricLocationLink> subfolders,
                     List<SymmetricLocationLink> files, SymmetricLink parent2meta, byte[] properties,
                     FileRetriever retriever, SymmetricLocationLink parentLink, Optional<SymmetricLocationLink> moreFolderContents) {
        super(parent2meta, properties, retriever, parentLink);
        this.subfolders2files = subfolders2files;
        this.subfolders2parent = subfolders2parent;
        this.subfolders = subfolders;
        this.files = files;
        this.moreFolderContents = moreFolderContents;
    }

    public DirAccess withNextBlob(Optional<SymmetricLocationLink> moreFolderContents) {
        return new DirAccess(subfolders2files, subfolders2parent, subfolders, files, parent2meta, properties,
                retriever, parentLink, moreFolderContents);
    }

    public void serialize(DataSink bout) throws IOException {
        super.serialize(bout);
        bout.writeArray(subfolders2parent.serialize());
        bout.writeArray(subfolders2files.serialize());
        bout.writeInt(0);
        bout.writeInt(this.subfolders.size());
        subfolders.forEach(x -> bout.writeArray(x.serialize()));
        bout.writeInt(this.files.size());
        files.forEach(x -> bout.writeArray(x.serialize()));
        bout.writeBoolean(moreFolderContents.isPresent());
        if (moreFolderContents.isPresent())
            bout.writeArray(moreFolderContents.get().serialize());
    }

    public List<SymmetricLocationLink> getSubfolders() {
        return Collections.unmodifiableList(subfolders);
    }

    public List<SymmetricLocationLink> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public boolean isDirty(SymmetricKey baseKey) {
        throw new IllegalStateException("Unimplemented!");
    }

    public CompletableFuture<Boolean> rename(ReadableFilePointer writableFilePointer, FileProperties newProps, UserContext context) {
        if (!writableFilePointer.isWritable())
            throw new IllegalStateException("Need a writable pointer!");
        SymmetricKey metaKey;
        SymmetricKey parentKey = subfolders2parent.target(writableFilePointer.baseKey);
        metaKey = this.getMetaKey(parentKey);
        byte[] metaNonce = metaKey.createNonce();
        DirAccess dira = new DirAccess(this.subfolders2files, this.subfolders2parent,
                this.subfolders, this.files, this.parent2meta,
                ArrayOps.concat(metaNonce, metaKey.encrypt(newProps.serialize(), metaNonce)),
                null,
                parentLink,
                moreFolderContents
        );
        return context.uploadChunk(dira, writableFilePointer.location, Collections.emptyList());
    }

    public CompletableFuture<DirAccess> addFileAndCommit(ReadableFilePointer targetCAP, SymmetricKey ourSubfolders,
                                                         ReadableFilePointer ourPointer, UserContext context) {
        return addFilesAndCommit(Arrays.asList(targetCAP), ourSubfolders, ourPointer, context);
    }

    public CompletableFuture<DirAccess> addFilesAndCommit(List<ReadableFilePointer> targetCAPs, SymmetricKey ourSubfolders,
                                                         ReadableFilePointer ourPointer, UserContext context) {
        if (subfolders.size() + files.size() + targetCAPs.size() > MAX_CHILD_LINKS_PER_BLOB) {
            return getNextMetablob(ourSubfolders, context).thenCompose(nextMetablob -> {
                if (nextMetablob.size() >= 1) {
                    ReadableFilePointer nextPointer = nextMetablob.get(0).filePointer;
                    DirAccess nextBlob = (DirAccess) nextMetablob.get(0).fileAccess;
                    return nextBlob.addFilesAndCommit(targetCAPs, ourSubfolders, nextPointer, context);
                } else {
                    // first fill this directory, then overflow into a new one
                    int freeSlots = MAX_CHILD_LINKS_PER_BLOB - subfolders.size() - files.size();
                    List<ReadableFilePointer> addToUs = targetCAPs.subList(0, freeSlots);
                    List<ReadableFilePointer> addToNext = targetCAPs.subList(freeSlots, targetCAPs.size());
                    return addFilesAndCommit(addToUs, ourSubfolders, ourPointer, context).thenCompose(newUs -> {
                        // create and upload new metadata blob
                        SymmetricKey nextSubfoldersKey = SymmetricKey.random();
                        SymmetricKey ourParentKey = subfolders2parent.target(ourSubfolders);
                        DirAccess next = DirAccess.create(nextSubfoldersKey, FileProperties.EMPTY,
                                parentLink.targetLocation(ourParentKey), parentLink.target(ourParentKey), ourParentKey);
                        byte[] nextMapKey = context.randomBytes(32);
                        Location nextLocation = ourPointer.getLocation().withMapKey(nextMapKey);
                        ReadableFilePointer nextPointer = new ReadableFilePointer(nextLocation, nextSubfoldersKey);
                        return next.addFilesAndCommit(addToNext, nextSubfoldersKey, nextPointer, context).thenCompose(nextBlob -> {
                            // re-upload us with the link to the next DirAccess
                            DirAccess withNext = newUs.withNextBlob(Optional.of(SymmetricLocationLink.create(ourSubfolders, nextSubfoldersKey, nextPointer.getLocation())));
                            return withNext.commit(ourPointer.getLocation(), context);
                        });
                    });
                }
            });
        } else {
            SymmetricKey filesKey = this.subfolders2files.target(ourSubfolders);
            for (ReadableFilePointer targetCAP : targetCAPs)
                this.files.add(SymmetricLocationLink.create(filesKey, targetCAP.baseKey, targetCAP.getLocation()));

            return commit(ourPointer.getLocation(), context)
                    .thenApply(x -> this);
        }
    }

    public CompletableFuture<DirAccess> addSubdirAndCommit(ReadableFilePointer targetCAP, SymmetricKey ourSubfolders,
                                                           ReadableFilePointer ourPointer, UserContext context) {
        return addSubdirsAndCommit(Arrays.asList(targetCAP), ourSubfolders, ourPointer, context);
    }
    // returns new version of this directory
    public CompletableFuture<DirAccess> addSubdirsAndCommit(List<ReadableFilePointer> targetCAPs, SymmetricKey ourSubfolders,
                                                           ReadableFilePointer ourPointer, UserContext context) {
        if (subfolders.size() + files.size() + targetCAPs.size() > MAX_CHILD_LINKS_PER_BLOB) {
            return getNextMetablob(ourSubfolders, context).thenCompose(nextMetablob -> {
                if (nextMetablob.size() >= 1) {
                    ReadableFilePointer nextPointer = nextMetablob.get(0).filePointer;
                    DirAccess nextBlob = (DirAccess) nextMetablob.get(0).fileAccess;
                    return nextBlob.addSubdirsAndCommit(targetCAPs, nextPointer.baseKey,
                            nextPointer.withWritingKey(ourPointer.location.writer), context);
                } else {
                    // first fill this directory, then overflow into a new one
                    int freeSlots = MAX_CHILD_LINKS_PER_BLOB - subfolders.size() - files.size();
                    List<ReadableFilePointer> addToUs = targetCAPs.subList(0, freeSlots);
                    List<ReadableFilePointer> addToNext = targetCAPs.subList(freeSlots, targetCAPs.size());
                    return addSubdirsAndCommit(addToUs, ourSubfolders, ourPointer, context).thenCompose(newUs -> {
                        // create and upload new metadata blob
                        SymmetricKey nextSubfoldersKey = SymmetricKey.random();
                        SymmetricKey ourParentKey = subfolders2parent.target(ourSubfolders);
                        DirAccess next = DirAccess.create(nextSubfoldersKey, FileProperties.EMPTY,
                                parentLink != null ? parentLink.targetLocation(ourParentKey) : null,
                                parentLink != null ? parentLink.target(ourParentKey) : null, ourParentKey);
                        byte[] nextMapKey = context.randomBytes(32);
                        ReadableFilePointer nextPointer = new ReadableFilePointer(ourPointer.location.withMapKey(nextMapKey), nextSubfoldersKey);
                        return next.addSubdirsAndCommit(addToNext, nextSubfoldersKey, nextPointer, context).thenCompose(x -> {
                        // re-upload us with the link to the next DirAccess
                        DirAccess withNextBlob = newUs.withNextBlob(Optional.of(SymmetricLocationLink.create(ourSubfolders,
                                nextSubfoldersKey, nextPointer.getLocation())));
                        return newUs.commit(ourPointer.getLocation(), context);
                        });
                    });
                }
            });
        } else {
            for (ReadableFilePointer targetCAP : targetCAPs)
                this.subfolders.add(SymmetricLocationLink.create(ourSubfolders, targetCAP.baseKey, targetCAP.getLocation()));

            return commit(ourPointer.getLocation(), context);
        }
    }

    private CompletableFuture<List<RetrievedFilePointer>> getNextMetablob(SymmetricKey subfoldersKey, UserContext context) {
        if (!moreFolderContents.isPresent())
            return CompletableFuture.completedFuture(Collections.emptyList());
        return context.retrieveAllMetadata(Arrays.asList(moreFolderContents.get()), subfoldersKey);
    }

    public CompletableFuture<Boolean> updateChildLink(ReadableFilePointer ourPointer, RetrievedFilePointer original,
                                                      RetrievedFilePointer modified, UserContext context) {
        removeChild(original, ourPointer, context);
        CompletableFuture<DirAccess> toUpdate;
        if (modified.fileAccess.isDirectory())
            toUpdate = addSubdirAndCommit(modified.filePointer, ourPointer.baseKey, ourPointer, context);
        else {
            toUpdate = addFileAndCommit(modified.filePointer, ourPointer.baseKey, ourPointer, context);
        }
        return toUpdate.thenCompose(newDirAccess -> context.uploadChunk(newDirAccess, ourPointer.getLocation(), Collections.emptyList()));
    }

    public CompletableFuture<Boolean> removeChild(RetrievedFilePointer childRetrievedPointer, ReadableFilePointer ourPointer, UserContext context) {
        if (childRetrievedPointer.fileAccess.isDirectory()) {
            this.subfolders = subfolders.stream().filter(e -> {
                try {
                    Location target = e.targetLocation(ourPointer.baseKey);
                    boolean keep = true;
                    if (Arrays.equals(target.getMapKey(), childRetrievedPointer.filePointer.location.getMapKey()))
                        if (Arrays.equals(target.writer.getPublicKeys(), childRetrievedPointer.filePointer.location.writer.getPublicKeys()))
                            if (Arrays.equals(target.owner.getPublicKeys(), childRetrievedPointer.filePointer.location.owner.getPublicKeys()))
                                keep = false;
                    return keep;
                } catch (TweetNaCl.InvalidCipherTextException ex) {
                    ex.printStackTrace();
                    return false;
                } catch (Exception f) {
                    return false;
                }
            }).collect(Collectors.toList());
        } else {
            files = files.stream().filter(e -> {
            SymmetricKey filesKey = subfolders2files.target(ourPointer.baseKey);
                try {
                    Location target = e.targetLocation(filesKey);
                    boolean keep = true;
                    if (Arrays.equals(target.getMapKey(), childRetrievedPointer.filePointer.location.getMapKey()))
                        if (Arrays.equals(target.writer.getPublicKeys(), childRetrievedPointer.filePointer.location.writer.getPublicKeys()))
                            if (Arrays.equals(target.owner.getPublicKeys(), childRetrievedPointer.filePointer.location.owner.getPublicKeys()))
                                keep = false;
                    return keep;
                } catch (TweetNaCl.InvalidCipherTextException ex) {
                    ex.printStackTrace();
                    return false;
                } catch (Exception f) {
                    return false;
                }
            }).collect(Collectors.toList());
        }
        return context.uploadChunk(this, ourPointer.getLocation(), Collections.EMPTY_LIST);
    }

    // 0=FILE, 1=DIR
    public byte getType() {
        return 1;
    }

    // returns [RetrievedFilePointer]
    public CompletableFuture<Set<RetrievedFilePointer>> getChildren(UserContext context, SymmetricKey baseKey) {
        CompletableFuture<List<RetrievedFilePointer>> subdirsFuture = context.retrieveAllMetadata(this.subfolders, baseKey);
        CompletableFuture<List<RetrievedFilePointer>> filesFuture = context.retrieveAllMetadata(this.files, this.subfolders2files.target(baseKey));

        CompletableFuture<List<RetrievedFilePointer>> moreChildrenFuture = moreFolderContents.isPresent() ?
                context.retrieveAllMetadata(Arrays.asList(moreFolderContents.get()), baseKey) :
                CompletableFuture.completedFuture(Collections.emptyList());

        return subdirsFuture.thenCompose(subdirs -> filesFuture.thenCompose(files -> moreChildrenFuture.thenCompose(moreChildrenSource -> {
            // this only has one or zero elements
            Optional<RetrievedFilePointer> any = moreChildrenSource.stream().findAny();
            CompletableFuture<Set<RetrievedFilePointer>> moreChildren = any.map(d -> ((DirAccess)d.fileAccess).getChildren(context, d.filePointer.baseKey))
                    .orElse(CompletableFuture.completedFuture(Collections.emptySet()));
            return moreChildren.thenApply(moreRetrievedChildren -> {
                Set<RetrievedFilePointer> results = Stream.concat(
                        Stream.concat(
                                subdirs.stream(),
                                files.stream()),
                        moreRetrievedChildren.stream())
                        .collect(Collectors.toSet());
                return results;
            });
        })
        )
        );
    }

    public Set<Location> getChildrenLocations(SymmetricKey baseKey) {
        SymmetricKey filesKey = this.subfolders2files.target(baseKey);
        return Stream.concat(subfolders.stream().map(d -> d.targetLocation(baseKey)),
                files.stream().map(f -> f.targetLocation(filesKey)))
                .collect(Collectors.toSet());
    }

    public SymmetricKey getParentKey(SymmetricKey subfoldersKey) {
        return this.subfolders2parent.target(subfoldersKey);
    }

    public SymmetricKey getFilesKey(SymmetricKey subfoldersKey) {
        return this.subfolders2files.target(subfoldersKey);
    }

    // returns pointer to new child directory
    public CompletableFuture<ReadableFilePointer> mkdir(String name, UserContext userContext, User writer, byte[] ourMapKey,
                                                       SymmetricKey baseKey, SymmetricKey optionalBaseKey,
                                                        boolean isSystemFolder, SafeRandom random) {
        SymmetricKey dirReadKey = optionalBaseKey != null ? optionalBaseKey : SymmetricKey.random();
        byte[] dirMapKey = new byte[32]; // root will be stored under this in the btree
        random.randombytes(dirMapKey, 0, 32);
        SymmetricKey ourParentKey = this.getParentKey(baseKey);
        Location ourLocation = new Location(userContext.user, writer, ourMapKey);
        DirAccess dir = DirAccess.create(dirReadKey, new FileProperties(name, 0, LocalDateTime.now(), isSystemFolder, Optional.empty()), ourLocation, ourParentKey, null);
        CompletableFuture<ReadableFilePointer> result = new CompletableFuture<>();
        userContext.uploadChunk(dir, new Location(userContext.user, writer, dirMapKey), Collections.emptyList()).thenAccept(success -> {
            if (success) {
                ReadableFilePointer ourPointer = new ReadableFilePointer(userContext.user, writer, ourMapKey, baseKey);
                ReadableFilePointer subdirPointer = new ReadableFilePointer(new Location(userContext.user, writer, dirMapKey), dirReadKey);
                addSubdirAndCommit(subdirPointer, baseKey, ourPointer, userContext)
                        .thenAccept(modified -> result.complete(new ReadableFilePointer(userContext.user, writer, dirMapKey, dirReadKey)));
            } else
                result.completeExceptionally(new IllegalStateException("Couldn't upload directory metadata!"));
        });
        return result;
    }

    public CompletableFuture<DirAccess> commit(Location ourLocation, UserContext userContext) {
        return userContext.uploadChunk(this, ourLocation, Collections.emptyList())
                .thenApply(x -> this);
    }

    public CompletableFuture<DirAccess> copyTo(SymmetricKey baseKey, SymmetricKey newBaseKey, Location parentLocation,
                            SymmetricKey parentparentKey, User entryWriterKey, byte[] newMapKey, UserContext context) {
        SymmetricKey parentKey = getParentKey(baseKey);
        FileProperties props = getFileProperties(parentKey);
        DirAccess da = DirAccess.create(newBaseKey, props, parentLocation, parentparentKey, parentKey);
        SymmetricKey ourNewParentKey = da.getParentKey(newBaseKey);
        Location ourNewLocation = new Location(context.user, entryWriterKey, newMapKey);

        return this.getChildren(context, baseKey).thenCompose(RFPs -> {
            // upload new metadata blob for each child and re-add child
            CompletableFuture<DirAccess> reduce = RFPs.stream().reduce(CompletableFuture.completedFuture(da), (dirFuture, rfp) -> {
                SymmetricKey newChildBaseKey = rfp.fileAccess.isDirectory() ? SymmetricKey.random() : rfp.filePointer.baseKey;
                byte[] newChildMapKey = new byte[32];
                context.crypto.random.randombytes(newChildMapKey, 0, 32);
                Location newChildLocation = new Location(context.user, entryWriterKey, newChildMapKey);
                return rfp.fileAccess.copyTo(rfp.filePointer.baseKey, newChildBaseKey,
                        ourNewLocation, ourNewParentKey, entryWriterKey, newChildMapKey, context)
                        .thenCompose(newChildFileAccess -> {
                            ReadableFilePointer ourNewPointer = new ReadableFilePointer(ourNewLocation.owner, entryWriterKey, newMapKey, newBaseKey);
                            ReadableFilePointer newChildPointer = new ReadableFilePointer(newChildLocation, newChildBaseKey);
                            if (newChildFileAccess.isDirectory())
                                return dirFuture.thenCompose(dirAccess ->
                                        dirAccess.addSubdirAndCommit(newChildPointer, newBaseKey, ourNewPointer, context));
                            else
                                return dirFuture.thenCompose(dirAccess ->
                                        dirAccess.addFileAndCommit(newChildPointer, newBaseKey, ourNewPointer, context));
                        });
            }, (a, b) -> a.thenCompose(x -> b)); // TODO Think about this combiner function
            return reduce;
        }).thenCompose(finalDir -> finalDir.commit(new Location(parentLocation.owner, entryWriterKey, newMapKey), context));
    }

    public static DirAccess deserialize(FileAccess base, DataSource bin) throws IOException {
        byte[] s2p = bin.readArray();
        byte[] s2f = bin.readArray();

        int nSharingKeys = bin.readInt();
        List<SymmetricLocationLink> files = new ArrayList<>(), subfolders = new ArrayList<>();
        int nsubfolders = bin.readInt();
        for (int i=0; i < nsubfolders; i++)
            subfolders.add(SymmetricLocationLink.deserialize(bin.readArray()));
        int nfiles = bin.readInt();
        for (int i=0; i < nfiles; i++)
            files.add(SymmetricLocationLink.deserialize(bin.readArray()));
        boolean hasMoreChildren = bin.readBoolean();
        Optional<SymmetricLocationLink> moreChildren = hasMoreChildren ? Optional.of(SymmetricLocationLink.deserialize(bin.readArray())) : Optional.empty();
        return new DirAccess(new SymmetricLink(s2f), new SymmetricLink(s2p),
                subfolders, files, base.parent2meta, base.properties, base.retriever, base.parentLink, moreChildren);
    }

    public static DirAccess create(SymmetricKey subfoldersKey, FileProperties metadata, Location parentLocation, SymmetricKey parentParentKey, SymmetricKey parentKey) {
        SymmetricKey metaKey = SymmetricKey.random();
        if (parentKey == null)
            parentKey = SymmetricKey.random();
        SymmetricKey filesKey = SymmetricKey.random();
        byte[] metaNonce = metaKey.createNonce();
        SymmetricLocationLink parentLink = parentLocation == null ? null : SymmetricLocationLink.create(parentKey, parentParentKey, parentLocation);
        return new DirAccess(SymmetricLink.fromPair(subfoldersKey, filesKey),
                SymmetricLink.fromPair(subfoldersKey, parentKey),
                new ArrayList<>(), new ArrayList<>(),
                SymmetricLink.fromPair(parentKey, metaKey),
                ArrayOps.concat(metaNonce, metaKey.encrypt(metadata.serialize(), metaNonce)),
                null,
                parentLink,
                Optional.empty()
        );
    }
}
