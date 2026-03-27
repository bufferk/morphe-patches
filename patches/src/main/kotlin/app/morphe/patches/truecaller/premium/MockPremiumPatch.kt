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
        // Locate the isPremium field reference from the AttributesDTO toString method.
        // The toString string starts with "AttributesDTO(isPremium=" which immediately
        // precedes the IGET_BOOLEAN that reads the isPremium field.
        val isPremiumField = AttributesDTOToStringFingerprint.method
            .findFieldFromToString("AttributesDTO(isPremium=")

        // Now build a constructor fingerprint that targets the specific IPUT_BOOLEAN
        // that writes isPremium in the AttributesDTO constructor.
        val attributesDTOConstructorFingerprint = Fingerprint(
            definingClass = AttributesDTOToStringFingerprint.originalClassDef.type,
            name = "<init>",
            returnType = "V",
            filters = listOf(
                fieldAccess(
                    opcode = Opcode.IPUT_BOOLEAN,
                    reference = isPremiumField
                )
            )
        )

        attributesDTOConstructorFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.last().index
                val register = getInstruction<TwoRegisterInstruction>(index).registerA

                // Overwrite the register that is about to be stored as isPremium with
                // const 1 (true) so the AttributesDTO is always constructed as premium.
                addInstructions(
                    index,
                    "const/4 v$register, 0x1"
                )
            }
        }
    }
}
