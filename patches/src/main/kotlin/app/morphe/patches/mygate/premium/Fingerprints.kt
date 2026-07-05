/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.mygate.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

// ── Core premium status gate ─────────────────────────────────────────────────────────────────────

/**
 * Fingerprint for KotlinUtils.Companion.z() — isPremiumUser().
 *
 * The single source of truth consumed by:
 *   - Every MygateAdLoader.setIsPremium() call  (pre-approval, allow-entries, activity-feed popup,
 *     visitor profile masthead, apartment screen, household tile)
 *   - ValidatedEntryAdapter toggle (premium-only "always approve" switch)
 *   - HouseholdFragment "cvAlwaysApproveEntries" card visibility
 *   - AllowEntriesFragment premium UI branch
 *   - HomeActivity premium badge refresh
 *   - ActivityFeedBasicFragment is_premium URL query param
 *   - ActivityFeedFragment & MultipleVisitorFragment URL query params
 *
 * Patching to always return true makes all of the above treat the user as premium.
 */
internal object IsPremiumUserFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/utilities/KotlinUtils\$Companion;",
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("USER_PREMIUM_PLAN", "SOCIETY_PREMIUM_PLAN")
)

/**
 * Fingerprint for UserProfile.getCombinedUserPlanStatus().
 *
 * Called directly by UpgradePlanFragment, SubscriptionPurchaseActivity, and HomeActivity
 * to determine which plan badge to show and whether to display the upgrade CTA.
 * Patched to always return "USER_PREMIUM_PLAN" so every caller sees premium.
 */
internal object GetCombinedUserPlanStatusFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/UserProfile;",
    name = "getCombinedUserPlanStatus",
    returnType = "Ljava/lang/String;",
    parameters = emptyList()
)

// ── Upgrade dialog / paywall bypass ──────────────────────────────────────────────────────────────

/**
 * Fingerprint for KotlinUtils.Companion.D(Context) — showUpgradePremiumDialog().
 *
 * This method is called when a non-premium user tries to use a premium-gated feature
 * (e.g. toggling "always approve entries" in ValidatedEntryAdapter). It inflates
 * layout_upgrade_premium and shows an AlertDialog prompting the user to upgrade.
 *
 * Identified by the unique "layout_upgrade_premium" inflation and the AlertDialog setup
 * inside the KotlinUtils$Companion class.
 * Patching it to return-void silences all upgrade-dialog popups app-wide.
 */
internal object ShowUpgradeDialogFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/utilities/KotlinUtils\$Companion;",
    returnType = "V",
    parameters = listOf("Landroidx/fragment/app/FragmentActivity;"),
    filters = listOf(
        app.morphe.patcher.methodCall(
            definingClass = "Lcom/mygate/user/databinding/LayoutUpgradePremiumBinding;",
            name = "a",
            returnType = "Lcom/mygate/user/databinding/LayoutUpgradePremiumBinding;",
            parameters = listOf("Landroid/view/View;")
        )
    )
)

/**
 * Fingerprint for UpgradePlanFragment's plan-status check block.
 *
 * At startup the fragment reads getUserPlan().getCode() and getSocietyHighestPlan().getCode()
 * to decide whether to show the "already subscribed" UI or the paywall CTA.
 * Patching to return-void prevents the paywall from rendering when the fragment opens.
 *
 * Identified by both "USER_PREMIUM_PLAN" and "SOCIETY_PREMIUM_PLAN" appearing together in
 * one V-returning method of UpgradePlanFragment.
 */
internal object UpgradePlanCheckFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/ui/UpgradePlanFragment;",
    returnType = "V",
    strings = listOf("USER_PREMIUM_PLAN", "SOCIETY_PREMIUM_PLAN")
)

// ── Frequent pre-approval feature gates ──────────────────────────────────────────────────────────

/**
 * Fingerprint for UserProfile.getPremiumFeatureList().
 *
 * We patch this to ensure it never returns null. If it's null, we initialize it with
 * a new PremiumFeatureCategory. This ensures the getters below are always called by
 * the UI fragments.
 */
internal object GetPremiumFeatureListFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/UserProfile;",
    name = "getPremiumFeatureList",
    returnType = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
    parameters = emptyList()
)

/**
 * Fingerprint for PremiumFeatureCategory.getFreqDelivery().
 */
internal object GetFreqDeliveryFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
    name = "getFreqDelivery",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList()
)

/**
 * Fingerprint for PremiumFeatureCategory.getSurpriseDelivery().
 */
internal object GetSurpriseDeliveryFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
    name = "getSurpriseDelivery",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList()
)

/**
 * Fingerprint for PremiumFeatureCategory.getFreqCab().
 */
internal object GetFreqCabFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
    name = "getFreqCab",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList()
)

/**
 * Fingerprint for PremiumFeatureCategory.getFreqVisitingHelp().
 */
internal object GetFreqVisitingHelpFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
    name = "getFreqVisitingHelp",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList()
)

/**
 * Fingerprint for PremiumFeatureCategory.getFreqGuest().
 */
internal object GetFreqGuestFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/userprofile/entity/PremiumFeatureCategory;",
    name = "getFreqGuest",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList()
)

// ── Notification fixes ───────────────────────────────────────────────────────────────────────────

/**
 * Fingerprint for CommonUtility.j0() (or similar) which creates the intent for flash notifications.
 * Patched to ignore premium status and always launch NotificationCampaignActivity so flash
 * notifications show up correctly on the lockscreen instead of just opening SplashActivity silently.
 */
internal object CommonUtilityNotificationIntentFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/utilities/CommonUtility;",
    returnType = "Landroid/content/Intent;",
    parameters = listOf("Lcom/mygate/user/modules/notifications/entity/ApprovalNotification;"),
    strings = listOf("approvalNotification")
)

/**
 * Fingerprint for ReadPref.f() which reads "tokenNotFound".
 * Patched to always return 0 so the Troubleshooting UI doesn't erroneously block testing.
 */
internal object ReadPrefTokenNotFoundFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/common/preferences/ReadPref;",
    returnType = "I",
    parameters = emptyList(),
    strings = listOf("tokenNotFound")
)

/**
 * Fingerprint for AppNotificationSettings.getEintercom().
 * We patch this to always return 1 so the app thinks e-Intercom is globally enabled.
 */
internal object AppNotificationSettingsGetEintercomFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/notifications/entity/AppNotificationSettings;",
    name = "getEintercom",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList()
)

/**
 * Fingerprint for NotificationSettings.getNotify().
 * We patch this to always return "1" so the troubleshooting UI bypasses the "enable e-Intercom" error.
 */
internal object NotificationSettingsGetNotifyFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/notifications/entity/NotificationSettings;",
    name = "getNotify",
    returnType = "Ljava/lang/String;",
    parameters = emptyList()
)

/**
 * Fingerprint for TestNotificationTroubleshootingViewModel.onNotificationSettingsManagerSuccess()
 * We override this to ignore the API response and always emit a valid NotificationSettings
 * object so the troubleshooting UI doesn't crash on e-Intercom check.
 */
internal object TroubleshootingSettingsSuccessFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/testnotification/ui/viewmodel/TestNotificationTroubleshootingViewModel;",
    name = "onNotificationSettingsManagerSuccess",
    returnType = "V",
    parameters = listOf("Lcom/mygate/user/modules/notifications/events/manager/INotificationSettingsManagerSuccessEvent;")
)

/**
 * Fingerprint for TestNotificationTroubleshootingViewModel.onNotificationSettingsManagerFailure()
 */
internal object TroubleshootingSettingsFailureFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/modules/testnotification/ui/viewmodel/TestNotificationTroubleshootingViewModel;",
    name = "onNotificationSettingsManagerFailure",
    returnType = "V",
    parameters = listOf("Lcom/mygate/user/modules/notifications/events/manager/INotificationSettingsManagerFailureEvent;")
)
