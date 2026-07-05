package app.morphe.patches.medium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.medium.shared.Constants.COMPATIBILITY_MEDIUM
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val freediumPatch = bytecodePatch(
    name = "Freedium",
    description = "Adds a profile setting to choose the Freedium Mirror host, and a floating button on articles that opens the current article using the chosen Freedium Mirror in a webview.",
    default = true
    compatibleWith(COMPATIBILITY_MEDIUM)
    extendWith("extensions/extension.rve")

    execute {
        // ── 1. SettingsFragment.F() ──────────────────────────────────────────────────
        // Intercept settings composition: wrap the returned ComposeView in a
        // LinearLayout containing a custom settings row to select the Freedium host name,
        // and store it in SharedPreferences.
        SettingsFragmentFFingerprint.method.apply {
            val instructions = implementation!!.instructions.toList()
            val returnInstruction = instructions.last { it.opcode == Opcode.RETURN_OBJECT }
            val index = instructions.indexOf(returnInstruction)
            val register = (returnInstruction as OneRegisterInstruction).registerA
            addInstructions(
                index,
                """
                    invoke-static {v$register}, Lapp/template/extension/extension/ExamplePatch;->wrapSettingsView(Landroid/view/View;)Landroid/view/View;
                    move-result-object v$register
                """.trimIndent()
            )
        }

        // ── 2. PostFragment.Q() ──────────────────────────────────────────────────────
        // Intercept post fragment view creation: add a floating button to the CoordinatorLayout
        // that launches a WebView Dialog displaying the current article fetched from the selected
        // Freedium Mirror.
        PostFragmentQFingerprint.method.apply {
            addInstructions(
                0,
                """
                    invoke-static {p0, p1}, Lapp/template/extension/extension/ExamplePatch;->onPostFragmentViewCreated(Ljava/lang/Object;Landroid/view/View;)V
                """.trimIndent()
            )
        }
    }
}
