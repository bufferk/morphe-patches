package app.morphe.patches.brave

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.rawResourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val enableBraveOriginPatch = rawResourcePatch(
    name = "Enable Brave Origin",
    description = "Enables Brave Origin features by default.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            name = "Brave Browser",
            packageName = "com.brave.browser",
            apkFileType = ApkFileType.APK,
            appIconColor = 0xFF4500,
            targets = listOf(
                AppTarget(version = null)
            )
        )
    )

    execute {
        val targetFile = "res/xml/_0_resource_name_obfuscated_res_0x7f180019.xml"
        val doc = document(targetFile)

        val switches = listOf(
            "rewards_switch",
            "privacy_preserving_analytics_switch",
            "email_aliases_switch",
            "leo_ai_switch",
            "news_switch",
            "statistics_reporting_switch",
            "vpn_switch",
            "wallet_switch",
            "web_discovery_project_switch"
        )

        val elements = doc.getElementsByTagName("*")
        for (i in 0 until elements.length) {
            val node = elements.item(i) as? Element ?: continue
            
            // The key might be in android namespace or without namespace depending on how Apktool decodes it
            val key = node.getAttribute("android:key").takeIf { it.isNotEmpty() } ?: node.getAttribute("key")
            
            if (key in switches) {
                if (node.hasAttribute("android:defaultValue") && node.getAttribute("android:defaultValue") == "false") {
                    node.setAttribute("android:defaultValue", "true")
                } else if (node.hasAttribute("defaultValue") && node.getAttribute("defaultValue") == "false") {
                    node.setAttribute("defaultValue", "true")
                }
            }
        }

        doc.close()
    }
}
