package app.morphe.patches.medium.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_MEDIUM = Compatibility(
        name = "Medium",
        packageName = "com.medium.reader",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x000000, // Black
        targets = listOf(
            AppTarget(
                version = "4.5.1302097",
                minSdk = 32,
            )
        )
    )
}
