/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.mygate.premium

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.mygate.shared.Constants.COMPATIBILITY_MYGATE

private const val USER_PREMIUM_PLAN = "USER_PREMIUM_PLAN"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks all MyGate premium features: spoofs USER_PREMIUM_PLAN status, " +
            "suppresses upgrade dialogs and paywalls, enables frequent pre-approvals for " +
            "deliveries, cabs and visiting help, unlocks surprise delivery and the " +
            "always-approve-entries toggle.",
    default = true
) {
    compatibleWith(COMPATIBILITY_MYGATE)

    execute {

        // ── 1. KotlinUtils.z() — isPremiumUser() ─────────────────────────────────────
        // Single source of truth for premium status, consumed by:
        //   • Every MygateAdLoader.setIsPremium() call (6 ad sites)
        //   • ValidatedEntryAdapter "always approve entries" toggle
        //   • HouseholdFragment "cvAlwaysApproveEntries" card
        //   • AllowEntriesFragment premium branch
        //   • HomeActivity premium badge & activity-feed URL params
        // Always return true → entire app sees the user as premium.
        IsPremiumUserFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // ── 2. UserProfile.getCombinedUserPlanStatus() ────────────────────────────────
        // Called directly by UpgradePlanFragment, SubscriptionPurchaseActivity, and
        // HomeActivity for plan-badge rendering and upgrade-CTA visibility.
        // Always return "USER_PREMIUM_PLAN" so every string comparison passes.
        GetCombinedUserPlanStatusFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$USER_PREMIUM_PLAN"
                return-object v0
            """
        )

        // ── 3. KotlinUtils.D(Context) — showUpgradePremiumDialog() ───────────────────
        // Shown whenever a non-premium user touches a locked feature (e.g. the
        // "always approve entries" toggle in ValidatedEntryAdapter).
        // return-void silences the popup for every call site app-wide.
        ShowUpgradeDialogFingerprint.method.addInstructions(
            0,
            "return-void"
        )

        // ── 4. UpgradePlanFragment plan-status gate ───────────────────────────────────
        // The fragment checks getUserPlan().getCode() at startup to decide whether to
        // show the paywall CTA or the "already subscribed" UI.
        // return-void prevents the paywall UI from ever being inflated.
        UpgradePlanCheckFingerprint.method.addInstructions(
            0,
            "return-void"
        )

        // ── 5. UserProfile.getPremiumFeatureList() ───────────────────────────────────
        // Used by DeliveryFragment, NotifyGateCabFragment, VisitingHelpApprovalFragment
        // to check limits. If null, the UI falls back to the upgrade path in some cases
        // (like surprise delivery).
        // Ensure we always return a valid object so the getters below are called.
        GetPremiumFeatureListFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/mygate/user/modules/userprofile/entity/UserProfile;->premiumFeatureCategory:Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;
                if-nez v0, :cond_0
                new-instance v0, Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;
                invoke-direct {v0}, Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;-><init>()V
                iput-object v0, p0, Lcom/mygate/user/modules/userprofile/entity/UserProfile;->premiumFeatureCategory:Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;
                :cond_0
                return-object v0
            """
        )

        // ── 6. PremiumFeatureCategory feature getters ────────────────────────────────
        // Force all frequent pre-approval settings to 1 (enabled).
        // This ensures the actual fragment UI naturally updates to the "unlocked" state
        // rather than us awkwardly skipping the UI update logic with return-void.
        val returnOneInteger = """
            const/4 v0, 0x1
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
        """.trimIndent()

        GetFreqDeliveryFingerprint.method.addInstructions(0, returnOneInteger)
        GetSurpriseDeliveryFingerprint.method.addInstructions(0, returnOneInteger)
        GetFreqCabFingerprint.method.addInstructions(0, returnOneInteger)
        GetFreqVisitingHelpFingerprint.method.addInstructions(0, returnOneInteger)
        GetFreqGuestFingerprint.method.addInstructions(0, returnOneInteger)

        // ── 7. Fix Flash Notifications Routing ───────────────────────────────────────
        // By forcing isPremiumUser to true globally, we accidentally routed flash
        // notifications to SplashActivity instead of NotificationCampaignActivity.
        // We override the Intent factory to explicitly return NotificationCampaignActivity.
        CommonUtilityNotificationIntentFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Landroid/content/Intent;
                sget-object v1, Lcom/mygate/user/app/AppController;->G:Lcom/mygate/user/app/AppController;
                invoke-virtual {v1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
                move-result-object v1
                const-class v2, Lcom/mygate/user/modules/notifications/ui/NotificationCampaignActivity;
                invoke-direct {v0, v1, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
                const-string v1, "approvalNotification"
                invoke-virtual {v0, v1, p0}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
                move-result-object v0
                return-object v0
            """
        )

        // ── 8. Fix "Test Notification" Troubleshooting Bug ───────────────────────────
        // We force ReadPref.f() (which reads tokenNotFound) to return 0 so the user
        // can successfully test notifications without being blocked by the "Google
        // Play Services" error UI, which trips if token generation ever fails.
        ReadPrefTokenNotFoundFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )

        // ── 9. Fix "e-Intercom" Settings & Troubleshooting Error ─────────────────────
        // We spoof both e-Intercom boolean getters so the app globally sees e-Intercom
        // as enabled, preventing the "Please enable e-Intercom" error in the Test
        // Notification UI and forcing the App Settings UI to consider it enabled.
        AppNotificationSettingsGetEintercomFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """
        )
        
        NotificationSettingsGetNotifyFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "1"
                return-object v0
            """
        )
        
        // ── 10. Fix Test Notification `e-Intercom` Null Pointer Check ───────────────
        // Even though we spoofed the e-Intercom boolean getters above, the MyGate API
        // may still fail to return a NotificationSettings payload due to our premium 
        // spoofing causing a backend signature/identity mismatch. When the payload is
        // null, the Troubleshooting UI's e-Intercom check (`zEquals = "1".equals(null)`)
        // evaluates to false and fails the entire testing flow. We patch the ViewModel's
        // success and failure methods to completely ignore the real API response and
        // ALWAYS emit a valid, non-null NotificationSettings object. This ensures `X` 
        // in the fragment is never null, allowing our boolean getters to do their job.
        val viewModelClass = classDefBy("Lcom/mygate/user/modules/testnotification/ui/viewmodel/TestNotificationTroubleshootingViewModel;")
        val liveDataField = viewModelClass.fields.first { it.name == "c" }
        val liveDataClassName = liveDataField.type
        
        val liveDataClass = classDefBy(liveDataClassName)
        val liveDataMethodName = liveDataClass.methods
            .firstOrNull { it.parameterTypes.size == 1 && it.parameterTypes[0] == "Ljava/lang/Object;" && it.returnType == "V" }
            ?.name ?: "l"

        val emitFakeNotificationSettings = """
            new-instance v0, Lcom/mygate/user/modules/notifications/entity/NotificationSettings;
            invoke-direct {v0}, Lcom/mygate/user/modules/notifications/entity/NotificationSettings;-><init>()V
            iget-object v1, p0, Lcom/mygate/user/modules/testnotification/ui/viewmodel/TestNotificationTroubleshootingViewModel;->c:$liveDataClassName
            invoke-virtual {v1, v0}, $liveDataClassName->$liveDataMethodName(Ljava/lang/Object;)V
            return-void
        """.trimIndent()

        TroubleshootingSettingsSuccessFingerprint.method.apply {
            removeInstructions(0)
            addInstructions(0, emitFakeNotificationSettings)
        }
        
        TroubleshootingSettingsFailureFingerprint.method.apply {
            removeInstructions(0)
            addInstructions(0, emitFakeNotificationSettings)
        }

        val appLiveDataField = TroubleshootingAppSettingsSuccessFingerprint.method.implementation!!.instructions
            .filterIsInstance<com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction>()
            .map { it.reference }
            .filterIsInstance<com.android.tools.smali.dexlib2.iface.reference.FieldReference>()
            .first()
        val appLiveDataFieldName = appLiveDataField.name
        val appLiveDataClassName = appLiveDataField.type

        val emitFakeAppNotificationSettings = """
            new-instance v0, Ljava/util/ArrayList;
            invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
            const/4 v1, 0x1
            invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v1
            new-instance v2, Lcom/mygate/user/modules/notifications/entity/AppNotificationSettings;
            const-string v3, ""
            invoke-direct {v2, v1, v3, v0}, Lcom/mygate/user/modules/notifications/entity/AppNotificationSettings;-><init>(Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;)V
            iget-object v1, p0, Lcom/mygate/user/modules/testnotification/ui/viewmodel/TestNotificationTroubleshootingViewModel;->$appLiveDataFieldName:$appLiveDataClassName
            invoke-virtual {v1, v2}, $appLiveDataClassName->$liveDataMethodName(Ljava/lang/Object;)V
            return-void
        """.trimIndent()

        TroubleshootingAppSettingsSuccessFingerprint.method.apply {
            removeInstructions(0)
            addInstructions(0, emitFakeAppNotificationSettings)
        }

        TroubleshootingAppSettingsFailureFingerprint.method.apply {
            removeInstructions(0)
            addInstructions(0, emitFakeAppNotificationSettings)
        }

        // ── 11. Spoof Firebase installations certificate fingerprint hash ────────────
        // Re-signing the APK breaks Firebase push notifications because Firebase checks
        // that the X-Android-Cert header matches the original certificate hash.
        // We override the header value at runtime to use the original developer cert's SHA-1.
        OpenHttpUrlConnectionFingerprint.method.apply {
            val originalCertHash = "AAB7E367980DA927FB146E862057CC87CD766987"
            
            // 1. Find the index of the instruction containing the string "X-Android-Cert"
            val xAndroidCertIndex = OpenHttpUrlConnectionFingerprint.stringMatches.firstOrNull()?.index
            if (xAndroidCertIndex != null) {
                // 2. Scan forward to find the next call to HttpURLConnection.addRequestProperty
                var addRequestPropertyInstruction: BuilderInstruction? = null
                for (i in xAndroidCertIndex until instructions.size) {
                    val instr = instructions[i] ?: continue
                    if (instr.opcode == Opcode.INVOKE_VIRTUAL) {
                        val ref = (instr as? ReferenceInstruction)?.reference as? MethodReference
                        if (ref?.name == "addRequestProperty") {
                            addRequestPropertyInstruction = instr
                            break
                        }
                    }
                }

                if (addRequestPropertyInstruction != null) {
                    // 3. Extract the register index for parameter 2 (registerE in FiveRegisterInstruction)
                    val valueRegister = (addRequestPropertyInstruction as FiveRegisterInstruction).registerE
                    
                    // 4. Inject const-string instruction before the invoke-virtual instruction to override the cert hash
                    addInstruction(
                        index = addRequestPropertyInstruction.location.index,
                        smaliInstructions = """
                            const-string v$valueRegister, "$originalCertHash"
                        """
                    )
                }
            }
        }
    }
}
