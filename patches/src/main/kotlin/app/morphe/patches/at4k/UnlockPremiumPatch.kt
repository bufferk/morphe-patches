package app.bounce.patches.at4k

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val AT4K = "com.overdevs.at4k"

val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks all AT4K premium features by forcing the premium status to true.",
    default = true,
) {
    compatibleWith(AT4K)

    execute {
        // Patch 1: O3.j(boolean) — the central premium setter.
        // Force p0 (the incoming boolean arg) to true before anything else runs.
        // This means every call to j(false) becomes j(true), including startup
        // state restoration and any billing failure path.
        SetPremiumStatusFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x1
            """
        )

        // Patch 2: MainActivity.onCreate — the initial SharedPreferences read.
        // Immediately call O3.j(true) before the app reads the stored is_premium
        // value, ensuring the in-memory LiveData is always initialised to true.
        MainActivityOnCreateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                invoke-static {v0}, LO3/O3;->j(Z)V
            """
        )
    }
}
