package app.morphe.patches.brave

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import org.w3c.dom.Element

// SharedPreferences key prefix for feature disable flags written by D1()
// and read by the feature gatekeeper methods.
// Key: "brave_origin_off_<policyKey>", value: true = user disabled this feature.
private const val PREF_PREFIX = "brave_origin_off_"

// Sets XML defaultValue="true" on all Origin preference switches so they
// display as ON before b5() runs. Merged from EnableBraveOriginPatch.
private val braveOriginResourcePatch = resourcePatch(
    name = "Brave Origin Resource Defaults",
    description = "Sets Origin preference switches to enabled by default.",
    default = false,
) {
    compatibleWith(
        Compatibility(
            name = "Brave Browser",
            packageName = "com.brave.browser",
            apkFileType = ApkFileType.APKM,
            appIconColor = 0xFF4500,
            targets = listOf(AppTarget(version = "1.92.140")),
        ),
    )
    execute {
        val switches = listOf(
            "rewards_switch",
            "privacy_preserving_analytics_switch",
            "email_aliases_switch",
            "leo_ai_switch",
            "news_switch",
            "statistics_reporting_switch",
            "vpn_switch",
            "wallet_switch",
            "web_discovery_project_switch",
        )
        // The preferences XML filename is obfuscated and changes between Brave versions.
        // Find the correct file by scanning all res/xml/*.xml files for our switch keys.
        val xmlDir = get("res/xml")
        val targetFile = xmlDir.listFiles()
            ?.filter { it.extension == "xml" }
            ?.firstOrNull { file ->
                file.readText().contains("rewards_switch")
            } ?: return@execute

        document(targetFile.absolutePath).use { doc ->
            val elements = doc.getElementsByTagName("*")
            for (i in 0 until elements.length) {
                val node = elements.item(i) as? Element ?: continue
                val key = node.getAttribute("android:key").takeIf { it.isNotEmpty() }
                    ?: node.getAttribute("key")
                if (key in switches) {
                    when {
                        node.hasAttribute("android:defaultValue") ->
                            node.setAttribute("android:defaultValue", "true")
                        node.hasAttribute("defaultValue") ->
                            node.setAttribute("defaultValue", "true")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val braveOriginPatch = bytecodePatch(
    name = "Brave Origin",
    description = "Unlocks Brave Origin and enables feature toggle controls.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            name = "Brave Browser",
            packageName = "com.brave.browser",
            apkFileType = ApkFileType.APKM,
            appIconColor = 0xFF4500,
            targets = listOf(AppTarget(version = "1.92.140")),
        ),
    )

    dependsOn(braveOriginResourcePatch)

    execute {

        // ── 1. v42.c(Profile) → true ──────────────────────────────────────────────────
        Fingerprint(
            returnType = "Z",
            parameters = listOf("Lorg/chromium/chrome/browser/profiles/Profile;"),
            strings = listOf(
                "getIsSubscriptionActive profile is null",
                "brave.origin.subscription_active_android",
            ),
        ).method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // ── 2. v42.d(Profile) → true ──────────────────────────────────────────────────
        val hasValidSubscriptionTokensFingerprint = Fingerprint(
            returnType = "Z",
            parameters = listOf("Lorg/chromium/chrome/browser/profiles/Profile;"),
            strings = listOf(
                "brave.origin.order_id_android",
                "brave.origin.purchase_token_android",
            ),
        )
        hasValidSubscriptionTokensFingerprint.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // ── 3. v42.b() → true ─────────────────────────────────────────────────────────
        Fingerprint(
            returnType = "Z",
            parameters = emptyList(),
            strings = listOf("brave_origin_credential_summary_cached"),
        ).method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // ── 4. v42.a(Profile, String) → skip ──────────────────────────────────────────
        Fingerprint(
            returnType = "V",
            parameters = listOf(
                "Lorg/chromium/chrome/browser/profiles/Profile;",
                "Ljava/lang/String;",
            ),
            strings = listOf(
                "brave.origin.package_name_android",
                "brave.origin.product_id_android",
            ),
        ).method.addInstructions(0, "return-void")

        // ── 4b. showOriginSettingsForRestart() → no-op ───────────────────────────────
        Fingerprint(
            definingClass = "Lorg/chromium/chrome/browser/brave_origin/BraveOriginSettingsLauncherHelper;",
            name = "showOriginSettingsForRestart",
            returnType = "V",
            parameters = emptyList(),
        ).method.addInstructions(0, "return-void")

        // ── 5. v42.f(Profile, Callback) → fire TRUE immediately ──────────────────────
        Fingerprint(
            returnType = "V",
            parameters = listOf(
                "Lorg/chromium/chrome/browser/profiles/Profile;",
                "Lorg/chromium/base/Callback;",
            ),
            strings = listOf(
                "requestCredentialSummary profile is null",
                "SkusService is null, cannot request credential summary",
            ),
        ).method.addInstructions(
            0,
            """
                sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-interface {p1, p0}, Lorg/chromium/base/Callback;->onResult(Ljava/lang/Object;)V
                return-void
            """,
        )

        // ── 6. Q4(): force v42.d call-site result = false ─────────────────────────────
        val validSubscriptionClass = hasValidSubscriptionTokensFingerprint.originalClassDef.type
        val validSubscriptionMethod = hasValidSubscriptionTokensFingerprint.originalMethod?.name
            ?: error("Failed to resolve Brave Origin credential predicate")
        val q4Fingerprint = Fingerprint(
            definingClass = "Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;",
            name = "Q4",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;"),
            filters = listOf(
                methodCall(
                    definingClass = validSubscriptionClass,
                    name = validSubscriptionMethod,
                    parameters = listOf("Lorg/chromium/chrome/browser/profiles/Profile;"),
                    returnType = "Z",
                ),
            ),
        )
        val validCheckIndex = q4Fingerprint.instructionMatches.first().index
        val resultReg = q4Fingerprint.method
            .getInstruction<OneRegisterInstruction>(validCheckIndex + 1).registerA
        q4Fingerprint.method.addInstructions(validCheckIndex + 2, "const/4 v$resultReg, 0x0")

        // ── 6b. Q4(): force J0=false to stop restart snackbar loop ────────────────────
        val q4RestartPromptFingerprint = Fingerprint(
            definingClass = "Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;",
            name = "Q4",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;"),
            filters = listOf(
                string("show_restart_prompt"),
                opcode(Opcode.IPUT_BOOLEAN),
            ),
        )
        val j0PutIndex = q4RestartPromptFingerprint.instructionMatches[1].index
        val j0PutReg = q4RestartPromptFingerprint.method
            .getInstruction<TwoRegisterInstruction>(j0PutIndex).registerA
        q4RestartPromptFingerprint.method.addInstructions(j0PutIndex, "const/4 v$j0PutReg, 0x0")

        // ── 7. D1(): write toggle state to SharedPreferences + show snackbar ──────────
        //
        //  Strategy: use Android SharedPreferences (SDK-only, no defpackage.* refs).
        //  Key: "brave_origin_off_<policyKey>", value: true = feature disabled.
        //  For *Disabled keys (Rewards/News/VPN/Wallet): store !switchValue
        //    → switch OFF means disabled = true.
        //  For *Enabled keys (P3A/Stats/WebDisc/AiChat): store !switchValue too
        //    → we normalize all keys as "off=true means feature disabled".
        //
        //  The 4 gatekeeper classes (d72/b12/lf2/pc2) are patched below to read
        //  these SharedPreferences values and return true when key is true.
        //
        //  Context: use getApplicationContext() via the fragment's Activity.
        val d1Fingerprint = Fingerprint(
            definingClass = "Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;",
            name = "D1",
            returnType = "Z",
            parameters = listOf("Landroidx/preference/Preference;", "Ljava/lang/Object;"),
        )
        d1Fingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            addInstructionsWithLabels(
                0,
                """
                    iget-boolean v0, p0, Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;->I0:Z
                    if-eqz v0, :not_locked
                    const/4 v0, 0x0
                    return v0
                    :not_locked
                    iget-object v0, p1, Landroidx/preference/Preference;->E:Ljava/lang/String;
                    invoke-static {v0}, Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;->Z4(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v0
                    if-eqz v0, :no_key
                    check-cast p2, Ljava/lang/Boolean;
                    invoke-virtual {p2}, Ljava/lang/Boolean;->booleanValue()Z
                    move-result v1
                    xor-int/lit8 v1, v1, 0x1
                    invoke-virtual {p0}, Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;->A4()Landroid/content/Context;
                    move-result-object v2
                    invoke-virtual {v2}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
                    move-result-object v2
                    const-string v3, "$PREF_PREFIX"
                    invoke-virtual {v3, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v3
                    invoke-static {v2}, Landroid/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;
                    move-result-object v2
                    invoke-interface {v2}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                    move-result-object v2
                    if-nez v1, :write_disabled
                    invoke-interface {v2, v3}, Landroid/content/SharedPreferences${'$'}Editor;->remove(Ljava/lang/String;)Landroid/content/SharedPreferences${'$'}Editor;
                    move-result-object v2
                    goto :write_done
                    :write_disabled
                    invoke-interface {v2, v3, v1}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                    move-result-object v2
                    :write_done
                    invoke-interface {v2}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                    invoke-virtual {p0}, Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;->c5()V
                    :no_key
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }

        // ── 8. b5(): set w=this via reflection, skip native n42.b() read ────────────────
        //
        //  b5() must set chromeSwitchPreference.w = this so D1() fires on toggle.
        //  Direct iput of Preference.w:nzc fails cross-DEX (ClassNotFoundException).
        //  Fix: use java.lang.reflect.Field to set w — pure Android SDK, always safe.
        //  Then return-void to skip the n42.b() native read (G0 is null anyway).
        val b5Fingerprint = Fingerprint(
            definingClass = "Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;",
            name = "b5",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;"),
        )
        b5Fingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0, p1}, Lorg/chromium/chrome/browser/settings/BraveOriginPreferences;->N4(Ljava/lang/CharSequence;)Landroidx/preference/Preference;
                move-result-object v0
                if-eqz v0, :b5_done
                move-object v3, p0
                const-string v1, "w"
                const-class v2, Landroidx/preference/Preference;
                invoke-virtual {v2, v1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;
                move-result-object v1
                const/4 v2, 0x1
                invoke-virtual {v1, v2}, Ljava/lang/reflect/Field;->setAccessible(Z)V
                invoke-virtual {v1, v0, v3}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V
                :b5_done
                return-void
            """,
        )

        // ── 9. Gatekeeper patches: read SharedPreferences to enforce feature state ─────
        //
        //  d72.a(Profile) = Rewards disabled check  → key: brave_origin_off_BraveRewardsDisabled
        //  b12.a(Profile) = News    disabled check  → key: brave_origin_off_BraveNewsDisabled
        //  lf2.a(Profile) = Wallet  disabled check  → key: brave_origin_off_BraveWalletDisabled
        //  pc2.a(Profile) = VPN     disabled check  → key: brave_origin_off_BraveVPNDisabled
        //
        //  These methods are in split_chrome.apk but the wrapper stub in base.apk only
        //  uses Android SDK classes (PreferenceManager, SharedPreferences) — safe.
        //
        //  Original: returns true when the native policy pref is set (feature disabled).
        //  Patched:  returns SharedPreferences value for our key (default false = enabled).

        // Helper smali: reads from default SharedPreferences and returns the boolean.
        // Context obtained via Application.getApplicationContext() — always available.
        // Uses android.app.ActivityThread.currentApplication() to get Context without
        // needing a reference — pure AOSP API available on all Android versions.
        fun gatekeeperSmali(prefKey: String) = """
                invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;
                move-result-object v0
                invoke-static {v0}, Landroid/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;
                move-result-object v0
                const-string v1, "${PREF_PREFIX}${prefKey}"
                const/4 v2, 0x0
                invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z
                move-result v0
                return v0
            """

        // d72: Rewards disabled check
        val rewardsGatekeeperFingerprint = Fingerprint(
            strings = listOf("brave.rewards.disabled_by_policy"),
        )
        rewardsGatekeeperFingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            addInstructions(0, gatekeeperSmali("BraveRewardsDisabled"))
        }

        // b12: News disabled check
        val newsGatekeeperFingerprint = Fingerprint(
            strings = listOf("brave.news.disabled_by_policy"),
        )
        newsGatekeeperFingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            addInstructions(0, gatekeeperSmali("BraveNewsDisabled"))
        }

        // lf2: Wallet disabled check
        val walletGatekeeperFingerprint = Fingerprint(
            strings = listOf("brave.wallet.disabled_by_policy"),
        )
        walletGatekeeperFingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            addInstructions(0, gatekeeperSmali("BraveWalletDisabled"))
        }

        // pc2: VPN disabled check
        val vpnGatekeeperFingerprint = Fingerprint(
            strings = listOf("brave.brave_vpn.disabled_by_policy"),
        )
        vpnGatekeeperFingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            addInstructions(0, gatekeeperSmali("BraveVPNDisabled"))
        }

        // lv1.c: Leo AI policy disabled check (e(key) && !b(key) = disabled)
        // We replace with the same SharedPreferences read pattern.
        val leoAIPolicyGatekeeperFingerprint = Fingerprint(
            strings = listOf("brave.ai_chat.enabled_by_policy"),
        )
        leoAIPolicyGatekeeperFingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            addInstructions(0, gatekeeperSmali("BraveAIChatEnabled"))
        }

        // lv1.d: Leo AI feature flag check (k73.b.f("AIChat"))
        // Returns false when user disabled Leo — this stops Leo from appearing in UI.
        // Unique identifier: it checks feature flag "AIChat" and has no Profile param.
        val leoAIFeatureFlagFingerprint = Fingerprint(
            returnType = "Z",
            parameters = emptyList(),
            strings = listOf("AIChat"),
        )
        leoAIFeatureFlagFingerprint.method.apply {
            removeInstructions(0, implementation!!.instructions.count())
            // lv1.d() has 0 params = only 2 registers (v0, v1). Use contains() — 2 args only.
            // D1() removes key on toggle ON and writes key on toggle OFF,
            // so contains() = true means Leo is disabled.
            addInstructions(
                0,
                """
                    invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;
                    move-result-object v0
                    invoke-static {v0}, Landroid/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;
                    move-result-object v0
                    const-string v1, "${PREF_PREFIX}BraveAIChatEnabled"
                    invoke-interface {v0, v1}, Landroid/content/SharedPreferences;->contains(Ljava/lang/String;)Z
                    move-result v0
                    if-nez v0, :leo_disabled
                    const/4 v0, 0x1
                    return v0
                    :leo_disabled
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}
