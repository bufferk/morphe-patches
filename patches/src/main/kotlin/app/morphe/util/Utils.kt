package app.morphe.util

import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import kotlin.math.abs

fun Method.findFieldFromToString(stringPrefix: String): FieldReference {
    val instructions = this.implementation?.instructions?.toList() 
        ?: throw IllegalStateException("Method has no implementation")
    
    var stringInstrIndex = -1
    val fieldIndices = mutableListOf<Pair<Int, FieldReference>>()
    
    instructions.forEachIndexed { index, instr ->
        if (instr is ReferenceInstruction) {
            val ref = instr.reference
            if (ref is StringReference && ref.string == stringPrefix) {
                stringInstrIndex = index
            } else if (ref is FieldReference) {
                fieldIndices.add(index to ref)
            }
        }
    }
    
    if (stringInstrIndex == -1) {
        throw IllegalStateException("String not found: $stringPrefix")
    }
    
    // Find the closest field reference 
    val closestField = fieldIndices.minByOrNull { abs(it.first - stringInstrIndex) }
    return closestField?.second ?: throw IllegalStateException("No field found near string $stringPrefix")
}
