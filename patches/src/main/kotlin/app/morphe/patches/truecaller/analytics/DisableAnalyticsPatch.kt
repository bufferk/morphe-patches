/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.analytics

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.truecaller.shared.Constants.COMPATIBILITY_TRUECALLER

/**
 * Disables Truecaller's behavioural analytics telemetry.
 *
 * Truecaller collects extensive user-behaviour data (call events, screen views,
 * spam-reports, contact lookups, feature usage) and ships it to:
 *
 *  • **CleverTap** (`kr0/k` — the concrete implementation of CleverTapManager):
 *    - `push(String)` — fires named events with no properties
 *    - `push(String, Map)` — fires named events with property maps
 *
 * Both dispatch methods are no-op'd here. Profile update and push-notification
 * registration are left intact because those don't carry behavioural metadata.
 *
 * Note: Firebase Analytics (crash reporting) and Adjust (attribution) are not
 * patched here because they are used for stability monitoring. If you want to
 * disable those as well, additional fingerprints will be needed.
 */
@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Suppresses Truecaller's CleverTap behavioural event tracking to prevent user-activity telemetry."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // No-op the bare event push (eventName only).
        // Insert "return-void" at offset 0 before any CleverTap SDK call is made.
        CleverTapPushEventFingerprint.method.addInstructions(
            0,
            """
                return-void
            """
        )

        // No-op the property-bearing event push (eventName + Map<String, Any>).
        CleverTapPushEventWithPropsFingerprint.method.addInstructions(
            0,
            """
                return-void
            """
        )
    }
}
