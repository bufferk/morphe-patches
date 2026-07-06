## [1.6.14](https://github.com/bufferk/morphe-patches/compare/v1.6.13...v1.6.14) (2026-07-06)

### 🐛 Bug Fixes

* **mygate:** use robust Dialog.show methodCall filter for ShowUpgradeDialogFingerprint to bypass log stripping ([7c8f763](https://github.com/bufferk/morphe-patches/commit/7c8f7637e7f3e2d11c9ff249154a36303751b755))

## [1.6.13](https://github.com/bufferk/morphe-patches/compare/v1.6.12...v1.6.13) (2026-07-05)

### 🐛 Bug Fixes

* **mygate:** make ShowUpgradeDialogFingerprint matching string-based to prevent obfuscated ViewBinding mismatch ([17eeddd](https://github.com/bufferk/morphe-patches/commit/17eeddd9708754d585976c56dc3ec02e64128f17))

## [1.6.12](https://github.com/bufferk/morphe-patches/compare/v1.6.11...v1.6.12) (2026-07-05)

### 🐛 Bug Fixes

* **mygate:** make ShowUpgradeDialogFingerprint dynamic to bypass method z obfuscation ([9fca981](https://github.com/bufferk/morphe-patches/commit/9fca981a6f7bab44aac16524806474d5d9f62381))

## [1.6.11](https://github.com/bufferk/morphe-patches/compare/v1.6.10...v1.6.11) (2026-07-05)

### 🐛 Bug Fixes

* **mygate:** resolve notification loss and fingerprint mismatches on 7.30.1 ([030d5ec](https://github.com/bufferk/morphe-patches/commit/030d5ecc0a48bf8965bde5e86991420d52ff736c))
* **workflow:** move clean task to start of build to prevent deleting release assets ([54bda4d](https://github.com/bufferk/morphe-patches/commit/54bda4d8d893af5d17abc545d73d8496e4222576))

## [1.6.11](https://github.com/bufferk/morphe-patches/compare/v1.6.10...v1.6.11) (2026-07-05)

### 🐛 Bug Fixes

* **mygate:** resolve notification loss and fingerprint mismatches on 7.30.1 ([030d5ec](https://github.com/bufferk/morphe-patches/commit/030d5ecc0a48bf8965bde5e86991420d52ff736c))

## [1.6.10](https://github.com/bufferk/morphe-patches/compare/v1.6.9...v1.6.10) (2026-07-05)

### 🐛 Bug Fixes

* **morphe:** merge latest patches template improvements and update patch list generator ([d834cd1](https://github.com/bufferk/morphe-patches/commit/d834cd1a80a6f19efa51b9da3403023856a8f313))

## [1.6.9](https://github.com/bufferk/morphe-patches/compare/v1.6.8...v1.6.9) (2026-07-05)


### Bug Fixes

* **medium:** add onLongClickListener to hide the Unlock button ([721fc87](https://github.com/bufferk/morphe-patches/commit/721fc871b369c4af04733b18545286bccf6824bb))

## [1.6.8](https://github.com/bufferk/morphe-patches/compare/v1.6.7...v1.6.8) (2026-07-05)


### Bug Fixes

* **medium:** prevent status bar overlap and adjust FAB bottom margin to 76dp ([57aca3b](https://github.com/bufferk/morphe-patches/commit/57aca3b063eeb0e15b5a1d61fb065b5cd5335875))

## [1.6.7](https://github.com/bufferk/morphe-patches/compare/v1.6.6...v1.6.7) (2026-07-05)


### Bug Fixes

* **medium:** add custom host option inside freedium settings dialog ([d5a553c](https://github.com/bufferk/morphe-patches/commit/d5a553c822370ce20c3f6263cdc0862a372f0955))

## [1.6.6](https://github.com/bufferk/morphe-patches/compare/v1.6.5...v1.6.6) (2026-07-05)


### Bug Fixes

* **medium:** set pill-shaped Unlock button with overlay and HTML loading animation inside webview ([6f2ac37](https://github.com/bufferk/morphe-patches/commit/6f2ac374c5907c357877197b61544ba0683bec04))

## [1.6.5](https://github.com/bufferk/morphe-patches/compare/v1.6.4...v1.6.5) (2026-07-05)


### Bug Fixes

* **medium:** refine Settings row integrations, click ripples, and floating button color/alignment ([a724c4f](https://github.com/bufferk/morphe-patches/commit/a724c4ffe33d033534e52de8d83e2ce13b619191))

## [1.6.4](https://github.com/bufferk/morphe-patches/compare/v1.6.3...v1.6.4) (2026-07-05)


### Bug Fixes

* **medium:** change extension file type to .mpe for Morphe compatibility ([a3400c4](https://github.com/bufferk/morphe-patches/commit/a3400c4491dca66cd907e160d57172cbb3bb5fc0))

## [1.6.3](https://github.com/bufferk/morphe-patches/compare/v1.6.2...v1.6.3) (2026-07-05)


### Bug Fixes

* **medium:** implement manual reflection-based extension loader to resolve classloader issues ([f3f536b](https://github.com/bufferk/morphe-patches/commit/f3f536bf78c015f2896ffac350573f6ce32a0959))

## [1.6.2](https://github.com/bufferk/morphe-patches/compare/v1.6.1...v1.6.2) (2026-07-05)


### Bug Fixes

* **medium:** correct syntax error in FreediumPatch.kt ([b85de4d](https://github.com/bufferk/morphe-patches/commit/b85de4ddfc46576495b607df04c9d3ccf0baba56))
* **medium:** declare extension dependency in patch builder DSL ([b9ddfb2](https://github.com/bufferk/morphe-patches/commit/b9ddfb20539840e51fb2e129cc01bd8f7069b225))

## [1.6.1](https://github.com/bufferk/morphe-patches/compare/v1.6.0...v1.6.1) (2026-07-05)


### Bug Fixes

* **medium:** inject at start of PostFragment.Q to prevent Verifier error ([de17b93](https://github.com/bufferk/morphe-patches/commit/de17b935495af8923a28becdcca99e9da428cd0f))

# [1.6.0](https://github.com/bufferk/morphe-patches/compare/v1.5.2...v1.6.0) (2026-07-05)


### Bug Fixes

* fix notifications ([fe555c8](https://github.com/bufferk/morphe-patches/commit/fe555c8d3c93fdf202b7296b4364c5a07329b61e))


### Features

* **medium:** add Freedium Mirror patch ([e0465c8](https://github.com/bufferk/morphe-patches/commit/e0465c86d101e91e10f1f79e1ce02e0036a3a805))

## [1.5.2](https://github.com/bufferk/morphe-patches/compare/v1.5.1...v1.5.2) (2026-05-09)


### Bug Fixes

* fix spoofed setting ([b1e3da0](https://github.com/bufferk/morphe-patches/commit/b1e3da0736e9bf67fd48766202b6eaedb318ede0))

## [1.5.1](https://github.com/bufferk/morphe-patches/compare/v1.5.0...v1.5.1) (2026-05-09)


### Bug Fixes

* mygate notifications ([ae55452](https://github.com/bufferk/morphe-patches/commit/ae55452a27083d3e78fe032446165a62f85cdfce))

# [1.5.0](https://github.com/bufferk/morphe-patches/compare/v1.4.1...v1.5.0) (2026-05-08)


### Features

* patch splitwise app ([1b905e8](https://github.com/bufferk/morphe-patches/commit/1b905e8021a02f19f420dd388221df0daa4c9ba9))

## [1.4.1](https://github.com/bufferk/morphe-patches/compare/v1.4.0...v1.4.1) (2026-05-07)


### Bug Fixes

* inject plan code and name to properly trick flutter into premium status ([3914f55](https://github.com/bufferk/morphe-patches/commit/3914f5501a3d2b2f6c5b7125d4e7e30763237bcc))

# [1.4.0](https://github.com/bufferk/morphe-patches/compare/v1.3.3...v1.4.0) (2026-05-07)


### Features

* bypass Flutter ad rendering via JSON spoofing & nuke native floating banners ([ac2c5d1](https://github.com/bufferk/morphe-patches/commit/ac2c5d19c9cd84006e4f92f4099751de9a602b83))

## [1.3.3](https://github.com/bufferk/morphe-patches/compare/v1.3.2...v1.3.3) (2026-05-07)


### Bug Fixes

* mygate patch ([7e2c582](https://github.com/bufferk/morphe-patches/commit/7e2c58224e2659870b68d13b56d8024299246562))

## [1.3.2](https://github.com/bufferk/morphe-patches/compare/v1.3.1...v1.3.2) (2026-05-07)


### Bug Fixes

* mygate patch fixed ([ee9a43e](https://github.com/bufferk/morphe-patches/commit/ee9a43e1dada05611173a7542dc9c1415b4898d0))

## [1.3.1](https://github.com/bufferk/morphe-patches/compare/v1.3.0...v1.3.1) (2026-05-07)


### Bug Fixes

* mygate patch fixed ([65b9206](https://github.com/bufferk/morphe-patches/commit/65b92062335a61e3dcb44ba548f89b5696c6135e))

# [1.3.0](https://github.com/bufferk/morphe-patches/compare/v1.2.0...v1.3.0) (2026-05-07)


### Features

* fix mygate issue ([a1d7ab9](https://github.com/bufferk/morphe-patches/commit/a1d7ab927f4826829296b4565b0690b7496743cf))
* my gate patch ([3df6460](https://github.com/bufferk/morphe-patches/commit/3df646006aaad405ff39878e43d7c9ec19e3970b))

# [1.2.0](https://github.com/bufferk/morphe-patches/compare/v1.1.0...v1.2.0) (2026-04-06)


### Features

* add one more patch ([5386df8](https://github.com/bufferk/morphe-patches/commit/5386df847dc86ed4c57aa36a05eb6a144a8c69c1))

# [1.1.0](https://github.com/bufferk/morphe-patches/compare/v1.0.5...v1.1.0) (2026-04-05)


### Features

* add more truecaller patches ([f91304c](https://github.com/bufferk/morphe-patches/commit/f91304cfeecd1b9ef94aaf08ed54a7f90d177644))

## [1.0.5](https://github.com/bufferk/morphe-patches/compare/v1.0.4...v1.0.5) (2026-04-05)


### Bug Fixes

* fix at4k patch ([4ccdf22](https://github.com/bufferk/morphe-patches/commit/4ccdf22c4d384518c282045ecaab96689ecadb32))

## [1.0.4](https://github.com/bufferk/morphe-patches/compare/v1.0.3...v1.0.4) (2026-04-05)


### Bug Fixes

* add gold tier to truecaller ([b15ca8e](https://github.com/bufferk/morphe-patches/commit/b15ca8ec021a2dd2e077c6d2098f0c37a7b203aa))

## [1.0.3](https://github.com/bufferk/morphe-patches/compare/v1.0.2...v1.0.3) (2026-04-05)


### Bug Fixes

* fix patch instructions ([363917d](https://github.com/bufferk/morphe-patches/commit/363917de5c944b72a8cfaa5dac18784100c240d6))

## [1.0.2](https://github.com/bufferk/morphe-patches/compare/v1.0.1...v1.0.2) (2026-04-05)


### Bug Fixes

* fix patch instructions ([7cb9773](https://github.com/bufferk/morphe-patches/commit/7cb9773c20802e12577c460e9faa6da843e8c155))

## [1.0.1](https://github.com/bufferk/morphe-patches/compare/v1.0.0...v1.0.1) (2026-04-05)


### Bug Fixes

* fix truecaller patch ([36a1c40](https://github.com/bufferk/morphe-patches/commit/36a1c4055086762e7ecc012e7b9abf660cf01770))

# 1.0.0 (2026-04-05)


### Bug Fixes

* fix missing file ([6aa5ff1](https://github.com/bufferk/morphe-patches/commit/6aa5ff13947f752a92de35158d8db04e8c536e17))
* fix patch generator ([3aa3aa6](https://github.com/bufferk/morphe-patches/commit/3aa3aa68eb6d45f73330bc4badccee01e6b8db5e))
* fixed truecaller patch ([20194d9](https://github.com/bufferk/morphe-patches/commit/20194d94cbd59759a32285ddea2620a085d42725))
* remove gpg check ([0a15d7b](https://github.com/bufferk/morphe-patches/commit/0a15d7b6a43ac1459396c48f6e3d937444781081))


### Features

* Added patches for at4k and bounce ([86886d0](https://github.com/bufferk/morphe-patches/commit/86886d0f0e42fa31b57e15862f30289579163f33))
