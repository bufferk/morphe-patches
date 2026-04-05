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

/**
 * Removes all "Get Premium" / "Switch to Premium" upgrade prompts from the UI.
 *
 * These prompts appear in three places:
 *  1. Bottom navigation bar — a "Get Gold" tab item
 *  2. Settings screen — an "Upgrade to Premium" list item
 *  3. Profile / nav drawer — a "Switch to Premium" banner
 *
 * All three launch one of the paywall Activity subclasses:
 *  • NavDrawerPaywallActivity
 *  • UpgradePathPaywallActivity
 *  • NonCarrierSupportPaywallActivity
 * …which all inherit `onCreate` from FullScreenPaywallActivity.
 *
 * Strategy:
 *  • Patch FullScreenPaywallActivity.onCreate to call finish() and return immediately,
 *    so any tap on an upgrade CTA dismisses the paywall screen instantly.
 *  • The visibility of the upgrade buttons themselves is driven by PremiumState.isPremium —
 *    with MockPremiumPatch force-returning true from k.b(), the app treats the user as
 *    premium and the nav/settings/profile CTAs should already be hidden. This patch
 *    acts as a safety net for any remaining call-sites that bypass that flow.
 */
@Suppress("unused")
val hidePremiumUpgradePromptsPatch = bytecodePatch(
    name = "Hide premium upgrade prompts",
    description = "Removes 'Get Premium' and 'Switch to Premium' buttons from the nav bar, settings, and profile."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // Immediately finish any paywall activity that manages to start.
        // finish() closes the Activity and simulates the back-press, so the user
        // never sees the upgrade screen regardless of which CTA they tapped.
        FullScreenPaywallOnCreateFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
            """
        )
    }
}
