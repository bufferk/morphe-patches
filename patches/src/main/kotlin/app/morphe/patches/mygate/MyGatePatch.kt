/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.mygate

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patches.mygate.shared.Constants.COMPATIBILITY_MYGATE
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// ── Internal: resolve the MutableLiveData field a ViewModel callback posts to ─────────────────
// 7.38.2 renamed the obfuscated LiveData fields, so instead of assuming a field name,
// scan the callback for the first MutableLiveData field reference.
private fun liveDataFieldOf(method: MutableMethod): FieldReference =
    method.implementation!!.instructions
        .filterIsInstance<ReferenceInstruction>()
        .mapNotNull { it.reference as? FieldReference }
        .first { it.type == "Landroidx/lifecycle/MutableLiveData;" }

// ── Internal: Flutter premium payload injection ───────────────────────────────────────────────
// Forces USER_PREMIUM_PLAN / SOCIETY_PREMIUM_PLAN into the JSON sent to the Flutter engine
// so the Flutter UI also sees the user as premium.
private val flutterPremiumPatch = bytecodePatch(
    name = "MyGate Flutter Premium",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MYGATE)
    execute {
        Fingerprint(
            definingClass = "Lcom/mygate/user/modules/userprofile/engine/UserProfileEngine;",
            name = "b",
            returnType = "V",
            parameters = listOf(
                "Lcom/mygate/user/modules/userprofile/engine/UserProfileEngine;",
                "Lorg/json/JSONObject;",
                "Z",
            ),
        ).method.addInstructions(
            0,
            """
            new-instance v0, Lorg/json/JSONObject;
            invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V
            const-string v1, "status"
            const-string v2, "ACTIVE"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "code"
            const-string v2, "USER_PREMIUM_PLAN"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "name"
            const-string v2, "Premium"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "user_plan"
            invoke-virtual {p1, v1, v0}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            new-instance v0, Lorg/json/JSONObject;
            invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V
            const-string v1, "status"
            const-string v2, "ACTIVE"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "code"
            const-string v2, "SOCIETY_PREMIUM_PLAN"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "name"
            const-string v2, "Premium"
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            const-string v1, "sort_index"
            const/4 v2, 0x3
            invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;
            const-string v1, "society_highest_plan"
            invoke-virtual {p1, v1, v0}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
            """,
        )
    }
}

// ── Internal: remove all in-app ad placements ────────────────────────────────────────────────
private val hideAdsPatch = bytecodePatch(
    name = "MyGate Hide Ads",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MYGATE)
    execute {
        // MygateAdLoader entry points — short-circuit every ad load call
        listOf(
            // loadAd() — pre-approval, allow-entries, activity-feed popup
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
                name = "loadAd",
                returnType = "V",
                parameters = listOf(
                    "Landroid/widget/ImageView;",
                    "Lcom/mygate/adsdk/ScratchView;",
                    "Landroid/widget/TextView;",
                    "Landroid/widget/FrameLayout;",
                ),
                strings = listOf("loadAd: "),
            ),
            // loadGlobalMastheadAd() — visitor profile card masthead (takes the ImageView since 7.38.2)
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
                name = "loadGlobalMastheadAd",
                returnType = "V",
                parameters = listOf("Landroid/widget/ImageView;"),
                strings = listOf("loadGlobalMastheadAd: "),
            ),
            // loadImageAd() — dashboard image/spotlight ad (renamed from loadDynamicSpotlightAd in 7.38.2)
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
                name = "loadImageAd",
                returnType = "V",
                parameters = listOf("Landroid/widget/ImageView;", "I", "I"),
            ),
            // getMyGateAdData (4-param) — background ad fetch
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
                name = "getMyGateAdData",
                returnType = "V",
                parameters = listOf(
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Lcom/mygate/adsdk/utils/MygateAdDataListener;",
                    "Z",
                ),
            ),
            // getMyGateAdData (7-param) — extended background ad fetch
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
                name = "getMyGateAdData",
                returnType = "V",
                parameters = listOf(
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Lcom/mygate/adsdk/utils/MygateAdDataListener;",
                    "Z",
                    "Z",
                ),
            ),
            // getMyGateMultiAdData — multi-ad carousel
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
                name = "getMyGateMultiAdData",
                returnType = "V",
                parameters = listOf(
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Lcom/mygate/adsdk/utils/MygateAdDataListener;",
                    "Z",
                    "Z",
                ),
            ),
            // FloatingBannerAdHandler — floating banner ads
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/ads/FloatingBannerAdHandler;",
                name = "loadAd",
                returnType = "V",
                parameters = listOf("Ljava/lang/String;", "Z"),
            ),
            Fingerprint(
                definingClass = "Lcom/mygate/adsdk/ads/FloatingBannerAdHandler;",
                name = "loadViews",
                returnType = "V",
                parameters = emptyList(),
            ),
        ).forEach { it.method.addInstructions(0, "return-void") }
    }
}

// ── Public: single MyGate patch ───────────────────────────────────────────────────────────────

private const val USER_PREMIUM_PLAN = "USER_PREMIUM_PLAN"

@Suppress("unused")
val myGatePatch = bytecodePatch(
    name = "MyGate",
    description = "Unlocks MyGate premium, removes ads, fixes notifications and e-Intercom.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MYGATE)
    dependsOn(flutterPremiumPatch, hideAdsPatch)

    execute {

        // ── 1. KotlinUtils.isPremiumUser() ────────────────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/utilities/KotlinUtils\$Companion;",
            returnType = "Z",
            parameters = emptyList(),
            strings = listOf("USER_PREMIUM_PLAN", "SOCIETY_PREMIUM_PLAN"),
        ).method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // ── 2. UserProfile.getCombinedUserPlanStatus() ────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/modules/userprofile/entity/UserProfile;",
            name = "getCombinedUserPlanStatus",
            returnType = "Ljava/lang/String;",
            parameters = emptyList(),
        ).method.addInstructions(0, "const-string v0, \"$USER_PREMIUM_PLAN\"\nreturn-object v0")

        // ── 3. KotlinUtils.showUpgradePremiumDialog() ─────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/utilities/KotlinUtils\$Companion;",
            returnType = "V",
            filters = listOf(
                methodCall(definingClass = "Landroid/app/Dialog;", name = "show", returnType = "V", parameters = emptyList()),
            ),
            custom = { method, _ -> method.parameters.size == 1 },
        ).method.addInstructions(0, "return-void")

        // ── 4. UpgradePlanFragment plan-status gate ───────────────────────────────────
        // In 7.38.2 the plan-status block lives inside onViewCreated(): it compares
        // getUserPlan()/getSocietyHighestPlan() codes against USER_PREMIUM_PLAN /
        // SOCIETY_PREMIUM_PLAN to pick the plan label (nuking the whole method with
        // return-void would break fragment init). Instead, force both equals() results
        // to true so the fragment always takes the premium-label branch.
        Fingerprint(
            definingClass = "Lcom/mygate/user/modules/userprofile/ui/UpgradePlanFragment;",
            returnType = "V",
            filters = listOf(
                string("USER_PREMIUM_PLAN"),
                string("SOCIETY_PREMIUM_PLAN"),
            ),
        ).let { fingerprint ->
            // Capture the mutable method ONCE: each `fingerprint.method` access can
            // allocate a new mutable copy, so all reads and writes must go through
            // the same instance.
            val method = fingerprint.method
            // Modify from the last match to the first so inserted instructions
            // don't shift the earlier match indexes.
            fingerprint.instructionMatches.reversed().forEach { match ->
                val instructions = method.instructions
                var moveResultIndex = -1
                var moveResultRegister = -1
                for (i in match.index + 1 until instructions.size) {
                    val instruction = instructions[i] ?: continue
                    if (instruction.opcode == Opcode.MOVE_RESULT) {
                        moveResultIndex = i
                        moveResultRegister = (instruction as OneRegisterInstruction).registerA
                        break
                    }
                }
                if (moveResultIndex >= 0) {
                    method.addInstructions(
                        moveResultIndex + 1,
                        "const/4 v$moveResultRegister, 0x1",
                    )
                } else {
                    check(false) {
                        "UpgradePlanFragment plan gate: no move-result found after plan string at index ${match.index}"
                    }
                }
            }
        }

        // ── 5. UserProfile.getPremiumFeatureList() — ensure non-null ──────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/modules/userprofile/entity/UserProfile;",
            name = "getPremiumFeatureList",
            returnType = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
            parameters = emptyList(),
        ).method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/mygate/user/modules/userprofile/entity/UserProfile;->premiumFeatureCategory:Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;
                if-nez v0, :cond_0
                new-instance v0, Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;
                invoke-direct {v0}, Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;-><init>()V
                iput-object v0, p0, Lcom/mygate/user/modules/userprofile/entity/UserProfile;->premiumFeatureCategory:Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;
                :cond_0
                return-object v0
            """,
        )

        // ── 6. PremiumFeatureCategory getters — force all limits to 1 ─────────────────
        val returnOne = "const/4 v0, 0x1\ninvoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\nmove-result-object v0\nreturn-object v0"
        listOf("getFreqDelivery", "getSurpriseDelivery", "getFreqCab", "getFreqVisitingHelp", "getFreqGuest").forEach { name ->
            Fingerprint(
                definingClass = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
                name = name,
                returnType = "Ljava/lang/Integer;",
                parameters = emptyList(),
            ).method.addInstructions(0, returnOne)
        }

        // ── 7. Fix flash notification routing ─────────────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/utilities/CommonUtility;",
            returnType = "Landroid/content/Intent;",
            parameters = listOf("Lcom/mygate/user/modules/notifications/entity/ApprovalNotification;"),
            strings = listOf("approvalNotification"),
        ).method.addInstructions(
            0,
            """
                new-instance v0, Landroid/content/Intent;
                sget-object v1, Lcom/mygate/user/app/AppController;->O:Lcom/mygate/user/app/AppController;
                invoke-virtual {v1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
                move-result-object v1
                const-class v2, Lcom/mygate/user/modules/notifications/ui/NotificationCampaignActivity;
                invoke-direct {v0, v1, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
                const-string v1, "approvalNotification"
                invoke-virtual {v0, v1, p0}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
                move-result-object v0
                return-object v0
            """,
        )

        // ── 8. Fix tokenNotFound troubleshooting gate ─────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/common/preferences/ReadPref;",
            returnType = "I",
            parameters = emptyList(),
            strings = listOf("tokenNotFound"),
        ).method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")

        // ── 9. Fix e-Intercom status spoof ────────────────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/mygate/user/modules/notifications/entity/AppNotificationSettings;",
            name = "getEintercom",
            returnType = "Ljava/lang/Integer;",
            parameters = emptyList(),
        ).method.addInstructions(0, returnOne)

        Fingerprint(
            definingClass = "Lcom/mygate/user/modules/notifications/entity/NotificationSettings;",
            name = "getNotify",
            returnType = "Ljava/lang/String;",
            parameters = emptyList(),
        ).method.addInstructions(0, "const-string v0, \"1\"\nreturn-object v0")

        // ── 10. Fix troubleshooting ViewModel — emit valid NotificationSettings ────────
        // 7.38.2: the success callback posts to a MutableLiveData field (name now
        // obfuscated, resolved dynamically below); the failure callback posts the
        // error String to a *different* LiveData whose observer check-casts to
        // String — posting settings there would crash. So on failure we emit the
        // fake settings to the *success* channel instead, letting troubleshooting
        // proceed as if the fetch had succeeded.
        val viewModelClassName =
            "Lcom/mygate/user/modules/testnotification/ui/viewmodel/TestNotificationTroubleshootingViewModel;"

        val settingsSuccessMethod = Fingerprint(
            definingClass = viewModelClassName,
            name = "onNotificationSettingsManagerSuccess",
            returnType = "V",
            parameters = listOf("Lcom/mygate/user/modules/notifications/events/manager/INotificationSettingsManagerSuccessEvent;"),
        ).method
        val settingsField = liveDataFieldOf(settingsSuccessMethod)
        val liveDataClassName = settingsField.type
        val liveDataMethodName = classDefBy(liveDataClassName).methods
            .firstOrNull { it.parameterTypes.size == 1 && it.parameterTypes[0] == "Ljava/lang/Object;" && it.returnType == "V" }
            ?.name ?: "l"

        val emitFakeSettings = """
            new-instance v0, Lcom/mygate/user/modules/notifications/entity/NotificationSettings;
            invoke-direct {v0}, Lcom/mygate/user/modules/notifications/entity/NotificationSettings;-><init>()V
            iget-object v1, p0, $viewModelClassName->${settingsField.name}:$liveDataClassName
            invoke-virtual {v1, v0}, $liveDataClassName->$liveDataMethodName(Ljava/lang/Object;)V
            return-void
        """.trimIndent()

        settingsSuccessMethod.apply {
            removeInstructions(instructions.size)
            addInstructions(0, emitFakeSettings)
        }

        Fingerprint(
            definingClass = viewModelClassName,
            name = "onNotificationSettingsManagerFailure",
            returnType = "V",
            parameters = listOf("Lcom/mygate/user/modules/notifications/events/manager/INotificationSettingsManagerFailureEvent;"),
        ).method.apply {
            removeInstructions(instructions.size)
            addInstructions(0, emitFakeSettings)
        }

        val appSettingsSuccessMethod = Fingerprint(
            definingClass = viewModelClassName,
            name = "onNotificationSettingsManagerSuccess",
            returnType = "V",
            parameters = listOf("Lcom/mygate/user/modules/notifications/events/manager/IGetAppNotificationSettingManagerSuccess;"),
        ).method
        val appSettingsField = liveDataFieldOf(appSettingsSuccessMethod)
        val appFieldName = appSettingsField.name
        val appFieldType = appSettingsField.type

        val emitFakeAppSettings = """
            iget-object v0, p0, $viewModelClassName->$appFieldName:$appFieldType
            new-instance v1, Lcom/mygate/user/modules/notifications/entity/AppNotificationSettings;
            const/4 v2, 0x0
            const-string v3, ""
            invoke-direct {v1, v2, v3, v2}, Lcom/mygate/user/modules/notifications/entity/AppNotificationSettings;-><init>(Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;)V
            invoke-virtual {v0, v1}, $appFieldType->$liveDataMethodName(Ljava/lang/Object;)V
            return-void
        """.trimIndent()

        appSettingsSuccessMethod.apply {
            removeInstructions(instructions.size)
            addInstructions(0, emitFakeAppSettings)
        }

        Fingerprint(
            definingClass = viewModelClassName,
            name = "onNotificationSettingsManagerFailure",
            returnType = "V",
            parameters = listOf("Lcom/mygate/user/modules/notifications/events/manager/IGetAppNotificationSettingManagerFailure;"),
        ).method.apply {
            removeInstructions(instructions.size)
            addInstructions(0, emitFakeAppSettings)
        }

        // ── 11. Spoof Firebase X-Android-Cert header ──────────────────────────────────
        // Re-signing changes the APK cert — Firebase installations validates it.
        // Override the cert hash at runtime to match the original developer SHA-1.
        Fingerprint(
            returnType = "Ljava/net/HttpURLConnection;",
            parameters = listOf("Ljava/net/URL;", "Ljava/lang/String;"),
            strings = listOf(
                "X-Android-Cert",
                "Firebase Installations Service is unavailable. Please try again later.",
            ),
        ).method.apply {
            val certHash = "AAB7E367980DA927FB146E862057CC87CD766987"
            val xAndroidCertIndex = Fingerprint(
                returnType = "Ljava/net/HttpURLConnection;",
                parameters = listOf("Ljava/net/URL;", "Ljava/lang/String;"),
                strings = listOf(
                    "X-Android-Cert",
                    "Firebase Installations Service is unavailable. Please try again later.",
                ),
            ).stringMatches.firstOrNull()?.index ?: return@apply

            var addRequestPropertyInstr: BuilderInstruction? = null
            for (i in xAndroidCertIndex until instructions.size) {
                val instr = instructions[i] ?: continue
                if (instr.opcode == Opcode.INVOKE_VIRTUAL) {
                    try {
                        val ref = instr.javaClass.methods.firstOrNull { it.name == "getReference" }?.invoke(instr)
                        val name = ref?.javaClass?.methods?.firstOrNull { it.name == "getName" }?.invoke(ref) as? String
                        if (name == "addRequestProperty") { addRequestPropertyInstr = instr; break }
                    } catch (_: Exception) {}
                }
            }
            addRequestPropertyInstr?.let {
                val reg = (it as FiveRegisterInstruction).registerE
                addInstruction(it.location.index, "const-string v$reg, \"$certHash\"")
            }
        }
    }
}
