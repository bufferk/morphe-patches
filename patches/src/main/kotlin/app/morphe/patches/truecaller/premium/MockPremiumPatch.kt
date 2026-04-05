/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.truecaller.shared.Constants.COMPATIBILITY_TRUECALLER
import app.morphe.util.findFieldFromToString
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val mockPremiumPatch = bytecodePatch(
    name = "Mock premium",
    description = "Always shows the premium/gold badge and unlocks premium UI features."
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // ── AttributesDTO ──────────────────────────────────────────────────────────
        // AttributesDTO.<init>(Z)V has a single IPUT_BOOLEAN for isPremium.
        // Locate the field reference via the toString string, then target the constructor.
        val isPremiumField = AttributesDTOToStringFingerprint.method
            .findFieldFromToString("AttributesDTO(isPremium=")

        val attributesDTOConstructorFingerprint = Fingerprint(
            definingClass = AttributesDTOToStringFingerprint.originalClassDef.type,
            name = "<init>",
            returnType = "V",
            filters = listOf(
                fieldAccess(opcode = Opcode.IPUT_BOOLEAN, reference = isPremiumField)
            )
        )

        attributesDTOConstructorFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.last().index
                val register = getInstruction<TwoRegisterInstruction>(index).registerA
                addInstructions(index, "const/4 v$register, 0x1")
            }
        }

        // ── k.b() — isPremium source of truth ─────────────────────────────────────
        // k.b() reads "isPremiumExpired" from SharedPrefs (default=true) and XORs
        // with 1 to invert it → returns false by default (not premium).
        // We replace the entire body with "return true" so the app always thinks
        // the user is premium. This is the single source feeding PremiumState.
        PremiumStatusPrefsFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // ── k.S1() — tier source of truth ─────────────────────────────────────────
        // k.S1() reads "premiumLevel" from SharedPrefs and returns a PremiumTierType.
        // We replace the body with "return GOLD" so the tier is always Gold.
        PremiumTierPrefsFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/truecaller/premium/data/tier/PremiumTierType;->GOLD:Lcom/truecaller/premium/data/tier/PremiumTierType;
                return-object v0
            """
        )

        // ── k.c1() — shouldShowAds source of truth ────────────────────────────────
        // k.c1() reads "shouldShowAds" from SharedPrefs (default=false).
        // We force it to always return false so the ad layer never activates.
        ShouldShowAdsPrefsFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )

        // ── PremiumState constructor ───────────────────────────────────────────────
        // Belt-and-suspenders: also patch the constructor in case PremiumState is
        // created from a cached/deserialized source path that bypasses k.b().
        PremiumStateConstructorFingerprint.method.apply {
            val instructions = implementation!!.instructions.toList()

            val isPremiumIndex = instructions.indexOfFirst { it.opcode == Opcode.IPUT_BOOLEAN }
            val tierIndex      = instructions.indexOfFirst { it.opcode == Opcode.IPUT_OBJECT }

            check(isPremiumIndex != -1) { "isPremium IPUT_BOOLEAN not found in PremiumState constructor" }
            check(tierIndex != -1)      { "tier IPUT_OBJECT not found in PremiumState constructor" }

            // Inject in reverse order so first insertion doesn't shift the second index.
            if (tierIndex > isPremiumIndex) {
                val tierRegister = getInstruction<TwoRegisterInstruction>(tierIndex).registerA
                addInstructions(
                    tierIndex,
                    "sget-object v$tierRegister, Lcom/truecaller/premium/data/tier/PremiumTierType;->GOLD:Lcom/truecaller/premium/data/tier/PremiumTierType;"
                )
                val isPremiumRegister = getInstruction<TwoRegisterInstruction>(isPremiumIndex).registerA
                addInstructions(isPremiumIndex, "const/4 v$isPremiumRegister, 0x1")
            } else {
                val isPremiumRegister = getInstruction<TwoRegisterInstruction>(isPremiumIndex).registerA
                addInstructions(isPremiumIndex, "const/4 v$isPremiumRegister, 0x1")
                val tierRegister = getInstruction<TwoRegisterInstruction>(tierIndex + 1).registerA
                addInstructions(
                    tierIndex + 1,
                    "sget-object v$tierRegister, Lcom/truecaller/premium/data/tier/PremiumTierType;->GOLD:Lcom/truecaller/premium/data/tier/PremiumTierType;"
                )
            }
        }
    }
}
