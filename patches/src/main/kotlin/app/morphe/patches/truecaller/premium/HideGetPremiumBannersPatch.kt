/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.truecaller.shared.Constants.COMPATIBILITY_TRUECALLER

@Suppress("unused")
val hideGetPremiumBannersPatch = bytecodePatch(
    name = "Hide 'Get Premium' banners",
    description = "Dismisses premium upgrade prompts and paywall screens automatically."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // Each paywall activity is immediately finished so it never shows to the user.
        // We inject finish() + return-void at the very start of onCreate so the activity
        // closes before any UI is inflated.
        val finishAndReturn = """
            invoke-virtual { p0 }, Landroid/app/Activity;->finish()V
            return-void
        """

        listOf(
            FullScreenPaywallOnCreateFingerprint
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, finishAndReturn)
        }
    }
}
