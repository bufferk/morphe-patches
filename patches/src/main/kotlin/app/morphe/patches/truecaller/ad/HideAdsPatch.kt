/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.ad

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.truecaller.shared.Constants.COMPATIBILITY_TRUECALLER

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Removes ads from the after-call screen and caller-ID overlay."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // Return early from the after-call screen ad update method so ads are never shown.
        AfterCallMaybeUpdateAdFingerprint.method.addInstructions(
            0,
            "return-void"
        )

        // Return early from the Neo (new UI) after-call screen ad update method.
        NeoAcsMaybeUpdateAdFingerprint.method.addInstructions(
            0,
            "return-void"
        )
    }
}
