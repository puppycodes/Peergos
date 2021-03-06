import file_system = require('../core/file_system');
import { ApiError } from '../core/api_error';
import file_flag = require('../core/file_flag');
import file = require('../core/file');
import { default as Stats } from '../core/node_fs_stats';
export default class WorkerFS extends file_system.BaseFileSystem implements file_system.FileSystem {
    private _worker;
    private _callbackConverter;
    private _isInitialized;
    private _isReadOnly;
    private _supportLinks;
    private _supportProps;
    private _outstandingRequests;
    constructor(worker: Worker);
    static isAvailable(): boolean;
    getName(): string;
    private _argRemote2Local(arg);
    _argLocal2Remote(arg: any): any;
    initialize(cb: () => void): void;
    isReadOnly(): boolean;
    supportsSynch(): boolean;
    supportsLinks(): boolean;
    supportsProps(): boolean;
    private _rpc(methodName, args);
    rename(oldPath: string, newPath: string, cb: (err?: ApiError) => void): void;
    stat(p: string, isLstat: boolean, cb: (err: ApiError, stat?: Stats) => void): void;
    open(p: string, flag: file_flag.FileFlag, mode: number, cb: (err: ApiError, fd?: file.File) => any): void;
    unlink(p: string, cb: Function): void;
    rmdir(p: string, cb: Function): void;
    mkdir(p: string, mode: number, cb: Function): void;
    readdir(p: string, cb: (err: ApiError, files?: string[]) => void): void;
    exists(p: string, cb: (exists: boolean) => void): void;
    realpath(p: string, cache: {
        [path: string]: string;
    }, cb: (err: ApiError, resolvedPath?: string) => any): void;
    truncate(p: string, len: number, cb: Function): void;
    readFile(fname: string, encoding: string, flag: file_flag.FileFlag, cb: (err: ApiError, data?: any) => void): void;
    writeFile(fname: string, data: any, encoding: string, flag: file_flag.FileFlag, mode: number, cb: (err: ApiError) => void): void;
    appendFile(fname: string, data: any, encoding: string, flag: file_flag.FileFlag, mode: number, cb: (err: ApiError) => void): void;
    chmod(p: string, isLchmod: boolean, mode: number, cb: Function): void;
    chown(p: string, isLchown: boolean, uid: number, gid: number, cb: Function): void;
    utimes(p: string, atime: Date, mtime: Date, cb: Function): void;
    link(srcpath: string, dstpath: string, cb: Function): void;
    symlink(srcpath: string, dstpath: string, type: string, cb: Function): void;
    readlink(p: string, cb: Function): void;
    syncClose(method: string, fd: file.File, cb: (e: ApiError) => void): void;
    static attachRemoteListener(worker: Worker): void;
}
