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
 * Fingerprint for the PremiumState constructor (obfuscated class zz1/n1).
 * Matched via the unique null-check strings "tier" and "productKind" that only
 * appear in this constructor body. The first IPUT_BOOLEAN is isPremium (field a:Z)
 * and the first IPUT_OBJECT is tier (field b:PremiumTierType).
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
