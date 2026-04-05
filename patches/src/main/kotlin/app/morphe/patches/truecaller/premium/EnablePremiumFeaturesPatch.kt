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
 * Unlocks all client-side premium features by patching the universal feature-gate method.
 *
 * Truecaller checks feature availability via:
 *     bar.a(featureList, PremiumFeature) → g02/e.a(PremiumFeatureStatus) → (status == INCLUDED)
 *
 * By patching g02/e.a() to unconditionally return true, every feature guarded by the
 * "availableFeatures" list is unlocked regardless of what the server returns, including:
 *   • callRecording (cloud + local)         • whoViewedMe / whoSearchedForMe
 *   • incognitoMode                         • ghostCall / callAssistant / announceCall
 *   • AICallScanner                         • contactRequest
 *   • extendedSpamBlocking                  • goldCallerId / verifiedBadge
 *   • premiumBadge / premiumSupport         • noAds / fraudInsurance / familySharing
 */
@Suppress("unused")
val enablePremiumFeaturesPatch = bytecodePatch(
    name = "Enable premium features",
    description = "Unlocks all client-side premium features by bypassing the feature-availability gate."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // g02/e.a(PremiumFeatureStatus) returns (status == INCLUDED).
        // Injecting "const/4 v0, 0x1; return v0" at offset 0 makes it always return true,
        // so every feature that calls bar.a(list, feature) is treated as available.
        IsFeatureIncludedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
