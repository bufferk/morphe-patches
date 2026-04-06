package app.morphe.patches.truecaller.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.truecaller.shared.Constants.COMPATIBILITY_TRUECALLER

@Suppress("unused")
val removePremiumUIPatch = bytecodePatch(
    name = "Remove Premium UI",
    description = "Removes the Premium tab from the bottom navigation bar and the Premium row in the profile section."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // Hide the Premium tab from the bottom navigation.
        // The method qn2/a.b()z determines if the premium button is added to the navigation bar.
        // We force it to always return false.
        val navPremiumTabMethod = Fingerprint(
            definingClass = Fingerprint(
                name = "<init>",
                strings = listOf("premiumPurchaseSupportedCheck", "premiumButton", "callsButton")
            ).definingClass,
            name = "b",
            returnType = "Z"
        ).method

        navPremiumTabMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )

        // Hide the "Switch to Premium" / Premium tier row in the Profile view entirely.
        // It's implemented via com.truecaller.premium.ui.PremiumNavDrawerItemView.
        // Setting its visibility to GONE (8) on attachment efficiently removes it.
        val profilePremiumRowMethod = Fingerprint(
            definingClass = "Lcom/truecaller/premium/ui/PremiumNavDrawerItemView;",
            name = "onAttachedToWindow",
            returnType = "V"
        ).method

        profilePremiumRowMethod.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p0, v0}, Landroid/view/View;->setVisibility(I)V
            """
        )
    }
}
