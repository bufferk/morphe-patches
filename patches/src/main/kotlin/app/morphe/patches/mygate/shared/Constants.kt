/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.mygate.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_MYGATE = Compatibility(
        name = "MyGate",
        packageName = "com.mygate.user",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(
                version = "7.31.0",
                minSdk = 26,
            ),
            AppTarget(
                version = "7.26.1",
                minSdk = 26,
            ),
        )
    )
}
