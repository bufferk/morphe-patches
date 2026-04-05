/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.truecaller.analytics

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val CLEVER_TAP_IMPL_CLASS = "Lkr0/k;"

/**
 * Fingerprint for CleverTapManager.push(String) — bare event tracking.
 *
 * This is the concrete implementation bound by CleverTapModule. Sends named
 * events with no properties to CleverTap's backend (e.g. "screen_viewed",
 * "call_made"). No-op-ing this silences all single-string event calls.
 */
internal object CleverTapPushEventFingerprint : Fingerprint(
    definingClass = CLEVER_TAP_IMPL_CLASS,
    name = "push",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * Fingerprint for CleverTapManager.push(String, Map) — keyed event tracking.
 *
 * Sends named events with a property map to CleverTap (e.g. call_duration,
 * spam_report details). No-op-ing this silences all property-bearing events.
 */
internal object CleverTapPushEventWithPropsFingerprint : Fingerprint(
    definingClass = CLEVER_TAP_IMPL_CLASS,
    name = "push",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
