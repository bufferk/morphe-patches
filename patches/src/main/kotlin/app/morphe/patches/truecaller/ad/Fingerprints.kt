/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.ad

import app.morphe.patcher.Fingerprint

/**
 * Fingerprint for the after-call screen maybeUpdateAd method.
 * In v26.10 the class is obfuscated to baz; maybeUpdateAd is obfuscated to Rh(Z)V.
 * We identify it via the unique "Soft throttle" string nearby in the same class,
 * and match on the method that creates baz$baz (the maybeUpdateAd lambda).
 */
internal object AfterCallMaybeUpdateAdFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/acs/ui/baz;",
    name = "Rh",
    returnType = "V",
    parameters = listOf("Z")
)

/**
 * Fingerprint for the Neo ACS maybeUpdateAd equivalent.
 * In v26.10 the class is obfuscated to tw1/f;
 * The method Th(Z)V is the equivalent loadAds/maybeUpdateAd entrypoint.
 */
internal object NeoAcsMaybeUpdateAdFingerprint : Fingerprint(
    definingClass = "Ltw1/f;",
    name = "Th",
    returnType = "V",
    parameters = listOf("Z")
)
