/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.splitwise.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_SPLITWISE = Compatibility(
        name = "Splitwise",
        packageName = "com.Splitwise.SplitwiseMobile",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1EC86A, // Splitwise green
        targets = listOf(
            AppTarget(
                version = "26.4.4",
                minSdk = 28,
            )
        )
    )
}
