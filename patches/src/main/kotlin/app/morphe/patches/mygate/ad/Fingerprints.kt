/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.mygate.ad

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

/**
 * Fingerprint for KotlinUtils.Companion.z() — the central isPremiumUser() gate.
 *
 * This static method is called by every ad load site before passing `isPremium`
 * to MygateAdLoader.setIsPremium(). Its body reads getCombinedUserPlanStatus()
 * and compares it against "USER_PREMIUM_PLAN" and "SOCIETY_PREMIUM_PLAN".
 *
 * We identify it uniquely by both plan code strings appearing together in one method.
 * Patching it to always return true both:
 *   - suppresses ad loading (MygateAdSdk skips ad fetch when isPremium=true), AND
 *   - doubles as the mock-premium signal used by UI gating checks.
 */
internal object IsPremiumUserFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/user/utilities/KotlinUtils\$Companion;",
    name = "z",
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("USER_PREMIUM_PLAN", "SOCIETY_PREMIUM_PLAN")
)

/**
 * Fingerprint for MygateAdLoader.loadAd() — the ad display entry point used by
 * PreApprovalPopUpFragment, AllowEntriesFragment, and ActivityFeedPopupFragment.
 *
 * This method takes (ImageView, ScratchView, TextView, FrameLayout?) and calls
 * mainViewModel.getSingleAd(..., isPremium) internally.
 * We short-circuit it entirely with return-void to prevent ad network calls.
 *
 * The unique identifier is the "loadAd: " debug log tag emitted at the top of the method.
 */
internal object LoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
    name = "loadAd",
    returnType = "V",
    parameters = listOf(
        "Landroid/widget/ImageView;",
        "Lcom/mygate/adsdk/ScratchView;",
        "Landroid/widget/TextView;",
        "Landroid/widget/FrameLayout;"
    ),
    strings = listOf("loadAd: ")
)

/**
 * Fingerprint for MygateAdLoader.loadGlobalMastheadAd() — used by VisitorProfileFragment
 * for the activity-card masthead ad slot.
 *
 * Identified by the "loadGlobalMastheadAd: " debug log string unique to this method.
 */
internal object LoadGlobalMastheadAdFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
    name = "loadGlobalMastheadAd",
    returnType = "V",
    strings = listOf("loadGlobalMastheadAd: ")
)

/**
 * Fingerprint for MygateAdLoader.loadDynamicSpotlightAd().
 */
internal object LoadDynamicSpotlightAdFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
    name = "loadDynamicSpotlightAd",
    returnType = "V",
    parameters = listOf("Landroid/widget/ImageView;")
)

/**
 * Fingerprint for MygateAdLoader.getMyGateAdData (4 params).
 */
internal object GetMyGateAdData4Fingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
    name = "getMyGateAdData",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;", 
        "Ljava/lang/String;", 
        "Lcom/mygate/adsdk/utils/MygateAdDataListener;", 
        "Z"
    )
)

/**
 * Fingerprint for MygateAdLoader.getMyGateAdData (7 params).
 */
internal object GetMyGateAdData7Fingerprint : Fingerprint(
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
        "Z"
    )
)

/**
 * Fingerprint for MygateAdLoader.getMyGateMultiAdData (6 params).
 */
internal object GetMyGateMultiAdDataFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/MygateAdLoader;",
    name = "getMyGateMultiAdData",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;", 
        "Ljava/lang/String;", 
        "Ljava/lang/String;", 
        "Lcom/mygate/adsdk/utils/MygateAdDataListener;", 
        "Z", 
        "Z"
    )
)

/**
 * Fingerprint for FloatingBannerAdHandler.loadAd().
 */
internal object LoadFloatingBannerAdFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/ads/FloatingBannerAdHandler;",
    name = "loadAd",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Z"
    )
)

/**
 * Fingerprint for FloatingBannerAdHandler.loadViews().
 */
internal object LoadViewsFloatingBannerAdFingerprint : Fingerprint(
    definingClass = "Lcom/mygate/adsdk/ads/FloatingBannerAdHandler;",
    name = "loadViews",
    returnType = "V",
    parameters = emptyList()
)
