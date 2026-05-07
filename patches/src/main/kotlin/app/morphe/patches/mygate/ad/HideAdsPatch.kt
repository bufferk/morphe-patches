/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.mygate.ad

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.mygate.shared.Constants.COMPATIBILITY_MYGATE

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Removes MyGate in-app ads (pre-approval screen, allow-entries screen, activity feed, and visitor profile masthead).",
    default = true
) {
    compatibleWith(COMPATIBILITY_MYGATE)

    execute {
        // ── loadAd() ────────────────────────────────────────────────────────────────
        // Short-circuit MygateAdLoader.loadAd() so no ad network request is made
        // and no ad view is ever attached to the UI for the pre-approval and
        // allow-entries screens.
        LoadAdFingerprint.method.addInstructions(
            0,
            "return-void"
        )

        // ── loadGlobalMastheadAd() ───────────────────────────────────────────────────
        // Short-circuit the global masthead ad slot used on the visitor profile card.
        LoadGlobalMastheadAdFingerprint.method.addInstructions(
            0,
            "return-void"
        )

        // ── Dashboard / Community Ads ────────────────────────────────────────────────
        // Short-circuit additional ad UI injection points and background ad fetches
        // which populate the dashboard (e.g. MultiAdCarouselFragment, ApartmentFragment).
        LoadDynamicSpotlightAdFingerprint.method.addInstructions(
            0,
            "return-void"
        )
        
        GetMyGateAdData4Fingerprint.method.addInstructions(
            0,
            "return-void"
        )
        
        GetMyGateAdData7Fingerprint.method.addInstructions(
            0,
            "return-void"
        )
        
        GetMyGateMultiAdDataFingerprint.method.addInstructions(
            0,
            "return-void"
        )
        
        LoadFloatingBannerAdFingerprint.method.addInstructions(
            0,
            "return-void"
        )
    }
}
