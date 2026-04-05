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
    accessFlags = listOf(AccessFlags.PUBLIC), // Removed FINAL because it's not final in newer versions
    strings = listOf("AttributesDTO(isPremium=")
)

/**
 * Fingerprint for the PremiumState toString method.
 * We use this to locate the core PremiumState class and resolve its fields.
 */
internal object PremiumStateToStringFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("PremiumState(isPremium=")
)

/**
 * Fingerprint for the NavDrawerPaywallActivity onCreate method.
 * This activity shows the "Get Premium" prompt from the navigation drawer.
 */
internal object NavDrawerPaywallOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/NavDrawerPaywallActivity;",
    name = "onCreate",
    returnType = "V"
)

/**
 * Fingerprint for the FullScreenPaywallActivity onCreate method.
 * This activity shows the full-screen premium upgrade paywall.
 */
internal object FullScreenPaywallOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/FullScreenPaywallActivity;",
    name = "onCreate",
    returnType = "V"
)

/**
 * Fingerprint for the UpgradePathPaywallActivity onCreate method.
 * This activity shows the upgrade path paywall screen.
 */
internal object UpgradePathPaywallOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/premium/UpgradePathPaywallActivity;",
    name = "onCreate",
    returnType = "V"
)
