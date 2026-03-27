/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.ad

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Fingerprint for the after-call screen (ACS) presenter ad loader.
 * Matches the loadAds method in AfterCallBasePresenter.
 */
internal object AfterCallLoadAdsFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/acs/ui/AfterCallBasePresenter;",
    name = "loadAds",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * Fingerprint for the ad refresh/update method in the after-call presenter.
 */
internal object AfterCallMaybeUpdateAdFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/acs/ui/AfterCallBasePresenter;",
    name = "maybeUpdateAd",
    returnType = "V"
)

/**
 * Fingerprint for the Neo (new) after-call screen presenter ad loader.
 */
internal object NeoAcsLoadAdsFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/neo/acs/ui/NeoAcsBasePresenter;",
    name = "loadAds",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
