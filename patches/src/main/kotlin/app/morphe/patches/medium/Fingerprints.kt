package app.morphe.patches.medium

import app.morphe.patcher.Fingerprint

internal object SettingsFragmentFFingerprint : Fingerprint(
    definingClass = "Lcom/medium/android/settings/main/SettingsFragment;",
    name = "F",
    returnType = "Landroid/view/View;",
    parameters = listOf(
        "Landroid/view/LayoutInflater;",
        "Landroid/view/ViewGroup;",
        "Landroid/os/Bundle;"
    )
)

internal object PostFragmentQFingerprint : Fingerprint(
    definingClass = "Lcom/medium/android/donkey/post/PostFragment;",
    name = "Q",
    returnType = "V",
    parameters = listOf("Landroid/view/View;")
)
