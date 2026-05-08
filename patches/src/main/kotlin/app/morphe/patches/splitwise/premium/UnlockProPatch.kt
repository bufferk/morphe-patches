/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.splitwise.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.splitwise.shared.Constants.COMPATIBILITY_SPLITWISE

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Spoofs Splitwise Pro subscription status everywhere it is checked — " +
            "bypasses the server-driven feature gate (charts, receipt scanning, currency " +
            "conversion, default splits, auto-split, transaction import, itemization), " +
            "forces Person.isPro() to true for receipt quality and UI gating, and " +
            "suppresses all in-app upsell ad banners (home-screen, recent activity, " +
            "post-add-expense). Works entirely client-side with no server interaction.",
    default = true
) {
    compatibleWith(COMPATIBILITY_SPLITWISE)

    execute {

        // ── 1. Person.isPro() ─────────────────────────────────────────────────────────
        // Used in: ImageCaptureScreen (receipt resolution), TransactionSourceAdjust-
        // AutoSplitModalFragment (auto-split CTA gating), and
        // ImportedTransactionSourceOnboardingScreen (onboarding flow gate).
        // Returning true makes every `person.isPro()` call report a pro user.
        IsProFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // ── 2. FeatureAdViewModel.accessFeature() — master pro-feature gate ──────────
        // Called from ALL pro-gated feature entry points:
        //   AddDetailFragment        → "receipt_scanning", "receipt_upload"
        //   ExpenseDetailsFragment   → "charts", "transaction_import", "receipt_upload"
        //   AddExpenseRootFragment   → "itemization", "default_splits"
        //   TransactionSourceAdjust… → "default_splits"
        //   AddExpenseScreen         → "post_add_bill_ad" (ad gate path)
        //   LiveSplitsHandlingActivity (indirectly via isSimpleFeatureEnabled)
        //
        // Original logic:
        //   if (adFeature.getVisible() && adFeature.getEnabled()) grantFeatureAccess()
        //   else showProAd(adFeature)
        //
        // By inserting `invoke-virtual {p0}, grantFeatureAccess()V ; return-void` at
        // offset 0 we short-circuit every feature access to "granted" before the server
        // metadata check runs, skipping the entire ad path.
        //
        // grantFeatureAccess() is a private method on the same class; it calls
        // `getFeatureAdResult().postValue(new FeatureAdRes(baseName, true, baseKey))`
        // which triggers the success callback registered in each Fragment's observer.
        AccessFeatureFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdViewModel;->grantFeatureAccess()V
                return-void
            """
        )

        // ── 3. AdFeatureStatus.getVisible() — in-app ad suppression ─────────────────
        // Returning false kills every `adFeature.getVisible() && ...` guard used to:
        //   - Decide whether to show "pro_root_screen_ad" banner on Balances/Activity/
        //     Account root screens (BalancesOverviewFragment, ActivityFragment,
        //     AccountFragment, RecentActivityScreen)
        //   - Decide whether to inject "recent_activity_ad" into the feed
        //     (RecentActivityScreen)
        //   - Decide whether to launch the "post_add_bill_ad" full-screen WebView
        //     after adding an expense (AddExpenseScreen, ExpenseAddedActivity)
        //   - Decide whether to display the inline "pro_settings_ad" in account settings
        //
        // We do NOT patch getEnabled() to false because accessFeature() (patched above)
        // bypasses getEnabled() entirely; patching it to false would be redundant for
        // the feature path but harmful for any usage that reads getEnabled() for non-ad
        // purposes (e.g. wallet card gating in NavigationRequestHandler).
        AdGetVisibleFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )

        // ── 4. FeatureAvailability.isSimpleFeatureEnabled() — secondary flag gate ────
        // Used for boolean "is this feature enabled?" queries that do not go through
        // the FeatureAdViewModel ad flow:
        //   - LiveSplitsHandlingActivity   — LIVE_SPLIT toggle
        //   - RecentPaymentRequestsProvider — "payment_requests" toggle
        //   - NavigationRequestHandler      — deeplink routing feature guards
        //   - UIUtilities                   — "app_store_review_prompt"
        //   - SplitwiseWalletCard* screens  — wallet feature flags
        //
        // Returning true makes all simple boolean capability checks pass without
        // consulting the server metadata cache.
        IsSimpleFeatureEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
