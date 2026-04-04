package app.bounce.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val BOUNCE_COMPANION = Compatibility(
        name = "Bounce Companion",
        packageName = "com.bounce.connect",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6600,
        targets = listOf(
            AppTarget(version = "7.0.7"),
            AppTarget(version = null) // Future versions
        )
    )
}
