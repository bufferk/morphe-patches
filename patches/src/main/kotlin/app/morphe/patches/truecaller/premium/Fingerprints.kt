/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Fingerprint for the AttributesDTO toString method.
 * The toString body contains "AttributesDTO(isPremium=" which lets us locate
 * the class and then resolve the isPremium field reference via findFieldFromToString.
 */
internal object AttributesDTOToStringFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC), // not FINAL in v26.10
    strings = listOf("AttributesDTO(isPremium=")
)

/**
 * Fingerprint for k.b() — the isPremium getter.
 *
 * Reads "isPremiumExpired" from SharedPreferences and XORs with 1 → false by default.
 * We patch to always return true (is premium).
 */
internal object PremiumStatusPrefsFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/data/k;",
    name = "b",
    returnType = "Z",
    strings = listOf("isPremiumExpired")
)

/**
 * Fingerprint for k.S1() — the premium tier getter.
 *
 * Reads "premiumLevel" from SharedPreferences (defaults to FREE).
 * We patch to always return GOLD.
 */
internal object PremiumTierPrefsFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/data/k;",
    name = "S1",
    returnType = "Lcom/truecaller/premium/data/tier/PremiumTierType;",
    strings = listOf("premiumLevel")
)

/**
 * Fingerprint for k.c1() — the shouldShowAds getter.
 *
 * Reads "shouldShowAds" from SharedPreferences.
 * We force to always return false.
 */
internal object ShouldShowAdsPrefsFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/data/k;",
    name = "c1",
    returnType = "Z",
    strings = listOf("shouldShowAds")
)

/**
 * Fingerprint for PremiumState (zz1/n1) constructor.
 * Belt-and-suspenders patch for cached/deserialized PremiumState paths.
 */
internal object PremiumStateConstructorFingerprint : Fingerprint(
    definingClass = "Lzz1/n1;",
    name = "<init>",
    returnType = "V",
    strings = listOf("tier", "productKind")
)

/**
 * Fingerprint for FullScreenPaywallActivity.onCreate.
 * Covers all paywall subclasses (NavDrawer, UpgradePath, NonCarrierSupport) via inheritance.
 */
internal object FullScreenPaywallOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/FullScreenPaywallActivity;",
    name = "onCreate",
    returnType = "V"
)

/**
 * Fingerprint for g02/e.a(PremiumFeatureStatus)Z — the universal feature-gate checker.
 *
 * Every client-side feature availability check funnels through:
 *   bar.a(featureList, PremiumFeature) → g02/e.a(status) → (status == INCLUDED)
 *
 * Patching this to always return true unlocks all features guarded by availableFeatures:
 *   callRecording, whoViewedMe, incognitoMode, ghostCall, callAssistant, announceCall,
 *   AICallScanner, contactRequest, extendedSpamBlocking, goldCallerId, verifiedBadge,
 *   premiumBadge, premiumSupport, noAds, fraudInsurance, familySharing, WhatsAppCallerId.
 *
 * Unique identifier: the tiny class body contains only a Kotlin null-check "<this>" string
 * and a single SGET of PremiumFeatureStatus.INCLUDED before the comparison.
 */
internal object IsFeatureIncludedFingerprint : Fingerprint(
    definingClass = "Lg02/e;",
    name = "a",
    returnType = "Z",
    parameters = listOf("Lcom/truecaller/premium/data/feature/PremiumFeatureStatus;"),
    strings = listOf("<this>")
)
