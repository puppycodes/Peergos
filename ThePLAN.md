The Peergos Plan
================
Fundamental aim: Release a P2P secure file storage, sharing and asynchronous commmunication platform.
---------------------

Launch 1 - Private Alpha (Basic File storage and sharing) 20/10/2015
-------------------------------------------------------------
See Issues.

Launch 2 - Public Alpha (KeyMail + file storage and sharing) 20/1/2016
------------------------------------------------------------------
- [ ] version keypair type (2)
- [ ] type property added to symmetrickey (2)
- [ ] chaining of directory metadata blocks (to support arbitrarily large directories)
- [ ] benchmarks, scalability tests (16)
- [ ] KeyMail (32)


Launch 3 - Beta 1 (KeyMail plus email interop) 6/2016
--------------------------------------------------------
- [ ] root key signing of release (including source to allow verifiable build), import root key into java+browser keystore, download from directory servers (16)
- [ ] auto update via root key signed releases (8)
- [ ] fragment storage verification (8)
- [ ] limits per sharing key settable by owner (8)
- [ ] outbound to email (single read/password/...) (unknown)
- [ ] inbound email (encrypted upon receipt, into size limited folder)
- [ ] security audit (40)


Launch 4 - Beta
---------------
- [ ] remove follow spam potential
- [ ] investigate tcp NAT traversal, webrtc, pjnath?
- [ ] transferral of earned storage rights between users
- [ ] facebook layer


- [ ] anonymising circuit construction ala Tor for sharing API (or just use Tor)
