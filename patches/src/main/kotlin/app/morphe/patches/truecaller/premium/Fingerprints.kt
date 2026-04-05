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
 * This is the SharedPreferences-backed source of truth for premium status.
 * It reads "isPremiumExpired" (defaults to true) and inverts it, so it returns
 * false (not premium) by default. We patch it to always return true.
 *
 * Class: com.truecaller.premium.data.k
 * Method: b()Z — unique because it reads "isPremiumExpired" then XORs with 1.
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
 * Reads "premiumLevel" from SharedPreferences (defaults to FREE) and returns
 * the corresponding PremiumTierType. We patch it to always return GOLD.
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
 * Reads "shouldShowAds" from SharedPreferences (defaults to false). We force
 * it to always return false so the ad system never activates regardless of
 * any network flag that might enable it.
 */
internal object ShouldShowAdsPrefsFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/data/k;",
    name = "c1",
    returnType = "Z",
    strings = listOf("shouldShowAds")
)

/**
 * Fingerprint for the PremiumState constructor (obfuscated class zz1/n1).
 * Matched via the unique null-check strings "tier" and "productKind" that only
 * appear in this constructor body. The first IPUT_BOOLEAN is isPremium (field a:Z)
 * and the first IPUT_OBJECT is tier (field b:PremiumTierType).
 * Used as a belt-and-suspenders patch for cached/deserialized PremiumState paths.
 */
internal object PremiumStateConstructorFingerprint : Fingerprint(
    definingClass = "Lzz1/n1;",
    name = "<init>",
    returnType = "V",
    strings = listOf("tier", "productKind")
)

/**
 * Fingerprint for the FullScreenPaywallActivity onCreate method.
 * NavDrawerPaywallActivity and UpgradePathPaywallActivity both inherit this method,
 * so patching it here covers all three paywall screens.
 */
internal object FullScreenPaywallOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/FullScreenPaywallActivity;",
    name = "onCreate",
    returnType = "V"
)
