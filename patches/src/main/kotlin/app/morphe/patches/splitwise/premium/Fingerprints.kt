/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.splitwise.premium

import app.morphe.patcher.Fingerprint

// ── User data model ─────────────────────────────────────────────────────────────────────────────

/**
 * Fingerprint for Person.isPro().
 *
 * `Person` is the data model for both the current user and friend list entries.
 * `isPro` is a boolean field (default false) populated from the `/current_user` API
 * response JSON key `"is_pro"`. It is consumed directly by:
 *   - ImageCaptureScreen — determines max receipt image resolution (pro gets full res)
 *   - TransactionSourceAdjustAutoSplitModalFragment — shows the auto-split configuration
 *     UI only if the user isPro, otherwise shows the pro upsell CTA
 *   - ImportedTransactionSourceOnboardingScreen — gating the transaction source onboarding
 *     flow to pro users only
 *
 * Patching to always return true makes every call site that reads `person.isPro()` see
 * a pro user regardless of what the server sent.
 */
internal object IsProFingerprint : Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/Person;",
    name = "isPro",
    returnType = "Z",
    parameters = emptyList()
)

// ── Central feature gate ─────────────────────────────────────────────────────────────────────────

/**
 * Fingerprint for FeatureAdViewModel.accessFeature(String, NavigationKey.SupportsPush).
 *
 * This is the SINGLE ENTRY POINT for every pro-gated feature access in the app:
 *   - Charts (group/friend/expense)                   — "charts" / "expense_details_chart"
 *   - Receipt scanning (camera OCR)                   — "receipt_scanning"
 *   - Receipt upload (image attachment on expense)    — "receipt_upload"
 *   - Default splits (pre-set split percentages)      — "default_splits"
 *   - Transaction import from bank                    — "transaction_import"
 *   - Itemization (split line items)                  — "itemization"
 *
 * When `getAdFeature(featureName).getVisible() && getEnabled()` is true, the method calls
 * `grantFeatureAccess()` which posts `FeatureAdRes(featureName, granted=true)` to the
 * LiveData. When false it calls `showProAd()` which shows a WebView upsell page.
 *
 * Patching to call `grantFeatureAccess()` unconditionally bypasses ALL of the above
 * server-driven feature gates in a single instruction change.
 *
 * Identified uniquely by the three private helper call string constants "grantFeatureAccess",
 * "denyFeatureAccess", "resetAccess" in the same class and the two-param public signature.
 */
internal object AccessFeatureFingerprint : Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdViewModel;",
    name = "accessFeature",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ldev/enro/core/NavigationKey\$SupportsPush;"
    ),
    strings = listOf("featureName")
)

// ── In-app ad suppression ─────────────────────────────────────────────────────────────────────────

/**
 * Fingerprint for AdFeatureStatus.getVisible().
 *
 * `AdFeatureStatus` is the data object returned by `FeatureAvailability.getAdFeature()` and
 * populated from the server metadata cache. Its `visible` flag controls whether an ad slot
 * is rendered. Ad features that use this flag:
 *   - "pro_root_screen_ad"  → banner shown on the Balances/Activity/Account root screens
 *                             when the user is NOT pro (BalancesOverviewFragment,
 *                             ActivityFragment, AccountFragment, RecentActivityScreen)
 *   - "recent_activity_ad"  → inline banner injected into the recent activity list
 *                             (RecentActivityScreen)
 *   - "post_add_bill_ad"    → full-screen ad shown after adding a new expense
 *                             (AddExpenseScreen, ExpenseAddedActivity)
 *   - "pro_settings_ad"     → ad in the account/settings screen for non-pro users
 *
 * Patching getVisible() to always return false causes every ad visibility check of the form
 * `adFeature.getVisible() && ...` to short-circuit to false, hiding all ad slots.
 *
 * NOTE: We intentionally do NOT patch `getEnabled()` here because `getEnabled()` is also
 * used by the pro-feature gating path in `FeatureAdViewModel.accessFeature()` (which we
 * patch separately via AccessFeatureFingerprint). Patching `getEnabled()` to false would
 * conflict with that patch by making all features appear disabled.
 *
 * Identified by the two overriding methods (getEnabled/getVisible) and the `hasAdToShow()`
 * helper that references neither "enabled" nor "visible" strings, making `getVisible` the
 * cleanest unique target.
 */
internal object AdGetVisibleFingerprint : Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;",
    name = "getVisible",
    returnType = "Z",
    parameters = emptyList()
)

// ── Simple feature flag gate ─────────────────────────────────────────────────────────────────────

/**
 * Fingerprint for FeatureAvailability.isSimpleFeatureEnabled(String).
 *
 * A secondary feature gate used for boolean "is this feature enabled?" checks that do NOT
 * require an ad-flow. Used for:
 *   - Live split detection                  — LIVE_SPLIT / "live_split"
 *   - Payment requests toggle               — "payment_requests"
 *   - Transaction import (secondary check)  — "transaction_import"
 *   - App store review prompt               — "app_store_review_prompt"
 *   - NavigationRequestHandler gating       — various feature names in deeplink routing
 *   - SAB improvements kill switch          — "sab_refresh_disable"
 *
 * Returns `defaultFeatureStatus.getEnabled() && defaultFeatureStatus.getVisible()`.
 * Patching to always return true causes all simple flag checks to pass.
 *
 * Identified by the unique log string "serializedMetadata" in the helper `loadFeaturesMetadata()`
 * which this method calls, plus the method's own two-statement body containing `loadFeatureData`
 * and a boolean AND of `getEnabled()` and `getVisible()`.
 */
internal object IsSimpleFeatureEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/utils/FeatureAvailability;",
    name = "isSimpleFeatureEnabled",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("name")
)
