<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Patches-Medium_|_Truecaller_|_Splitwise_|_MyGate-8A2BE2?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-GPLv3-blue?style=for-the-badge" />
</p>

<h1 align="center">🧩 Custom Morphe Patches</h1>

<p align="center">
  Custom Morphe patches for Android — premium unlocks, ad-free layouts, license bypasses, and UX enhancements for Medium, Truecaller, Splitwise, MyGate, and more.
</p>

<p align="center">
  <a href="https://github.com/bufferk/morphe-patches/discussions"><img src="https://img.shields.io/badge/Discussions-Join_Community-6e5494?style=flat-square&logo=github" /></a>
  <a href="https://github.com/bufferk/morphe-patches/releases"><img src="https://img.shields.io/badge/Releases-Latest-green?style=flat-square&logo=github" /></a>
</p>

---

## 📖 About

This repository provides custom, community-developed bytecode patches for various Android apps using the **Morphe Patcher** framework. It targets popular apps to restore premium styling, bypass verification checks, nuke tracking, and clean up visual overlays.

---

## ✨ Features

<table>
<tr>
<td width="50%" valign="top">

### 📰 Medium (Freedium Mirror)
- **Instant Article Unlock** — Floating pill button to open premium articles in a clean, ad-free WebView.
- **Premium HTML Loader** — Waving circle loader themed dynamically to light/dark mode.
- **Native Settings Option** — Settings item integrated into the account menu with native click ripples.
- **Custom Host Config** — Pick preconfigured hosts or input your own server (e.g. self-hosted mirrors) with clean input validation.
- **Dismiss on Hold** — Long-press the button to instantly hide it for the current article.

### 📞 Truecaller
- **Premium Unlocked** — Bypasses paywall logic and grants Gold/Premium tier features.
- **Nuke Upsells** — Removes all "Get Premium" banners, billing sheets, and upgrade dialogs.
- **Clean Layouts** — Disables native ads, promotion banners, and neo paywalls.
- **Privacy Enforcement** — Disables CleverTap tracking and analytics event logging.

</td>
<td width="50%" valign="top">

### 💵 Splitwise
- **Pro Features Unlocked** — Full access to Pro features (custom splits, receipt scanning, currency conversion).
- **Ad-Free UI** — Interstitials and banner advertisements completely disabled.

### 🛡️ MyGate
- **Premium Status** — Bypasses subscription checks and tricks Flutter-based layers to active plan status.
- **Hidden Ads** — Spotlight banners, global masthead promotions, and inline views fully disabled.

### 🔑 Core & License Bypasses
- **Bypass License Check** — Disables standard piracy checkers/license verifiers.
- **AT4K** — Unlocks premium features and removes activation popups.

</td>
</tr>
</table>

---

## 🎮 How to Use (Medium Freedium Feature)

| Gesture / Action | Result |
|:---|:---|
| **Tap** the floating `Unlock` button | Slides up a clean WebView reading dialog loading the article from the Freedium mirror. |
| **Long-Press** the floating `Unlock` button | Dismisses/hides the button instantly from the screen for the current reading session. |
| **Tap** the `Freedium Mirror Server` settings row | Opens the selection dialog to switch hosts or add a custom host with ripple animation. |

---

## 🩹 Patches

<!-- PATCHES_START EXPANDED -->
> **[v1.6.15-dev.6](https://github.com/bufferk/morphe-patches/releases/tag/v1.6.15-dev.6)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;14 patches total
<details open>
<summary>📦 Bounce Companion&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 7.0.7 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bypass license check](#bypass-license-check) | Bypasses the Google Play LVL backend license check, allowing full app access without activation. |  |

</details>

<details open>
<summary>📦 Truecaller&nbsp;&nbsp;•&nbsp;&nbsp;7 patches</summary>
<br>

**🎯 Supported versions:**

| 26.10.6 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable analytics](#disable-analytics) | Suppresses Truecaller's CleverTap behavioural event tracking to prevent user-activity telemetry. |  |
| [Enable premium features](#enable-premium-features) | Unlocks all client-side premium features by bypassing the feature-availability gate. |  |
| [Hide 'Get Premium' banners](#hide-get-premium-banners) | Dismisses premium upgrade prompts and paywall screens automatically. |  |
| [Hide ads](#hide-ads) | Removes ads from the after-call screen and caller-ID overlay. |  |
| [Hide premium upgrade prompts](#hide-premium-upgrade-prompts) | Removes 'Get Premium' and 'Switch to Premium' buttons from the nav bar, settings, and profile. |  |
| [Mock premium](#mock-premium) | Always shows the premium/gold badge and unlocks premium UI features. |  |
| [Remove Premium UI](#remove-premium-ui) | Removes the Premium tab from the bottom navigation bar and the Premium row in the profile section. |  |

</details>

<details open>
<summary>📦 MyGate&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 7.26.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Flutter Premium Bypass](#flutter-premium-bypass) | Forces premium status inside the JSON payload sent to Flutter |  |
| [Hide ads](#hide-ads) | Removes MyGate in-app ads (pre-approval screen, allow-entries screen, activity feed, and visitor profile masthead). |  |
| [Unlock premium](#unlock-premium) | Unlocks all MyGate premium features: spoofs USER_PREMIUM_PLAN status, suppresses upgrade dialogs and paywalls, enables frequent pre-approvals for deliveries, cabs and visiting help, unlocks surprise delivery and the always-approve-entries toggle. |  |

</details>

<details open>
<summary>📦 Medium&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 4.5.1302097 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Freedium](#freedium) | Adds a profile setting to choose the Freedium Mirror host, and a floating button on articles that opens the current article using the chosen Freedium Mirror in a webview. |  |

</details>

<details open>
<summary>📦 Splitwise&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 26.4.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Spoofs Splitwise Pro subscription status everywhere it is checked — bypasses the server-driven feature gate (charts, receipt scanning, currency conversion, default splits, auto-split, transaction import, itemization), forces Person.isPro() to true for receipt quality and UI gating, and suppresses all in-app upsell ad banners (home-screen, recent activity, post-add-expense). Works entirely client-side with no server interaction. |  |

</details>

<details open>
<summary>📦 com.overdevs.at4k&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock premium](#unlock-premium) | Unlocks all AT4K premium features by forcing the premium status to true. |  |

</details>

<!-- PATCHES_END -->

---

## 🛠️ Installation

### Option 1 · Morphe Manager <sup>Recommended</sup>

1. Install [**Morphe Manager**](https://morphe.software) on your Android device.
2. Add this repository as a patch source:

   <p align="center">
     <a href="https://morphe.software/add-source?github=bufferk/morphe-patches"><b>➕ Add Patches to Morphe Manager</b></a>
   </p>

   Or manually paste `https://github.com/bufferk/morphe-patches` in **Settings -> Patch Sources**.

3. Select your target app (e.g. **Medium**), choose your patches, and tap **Patch**.
4. Install the output APK.

### Option 2 · Morphe CLI <sup>Advanced</sup>

1. Assemble the patches bundle jar file:
   ```bash
   ./gradlew patches:buildAndroid
   ```
2. Run the patcher CLI to apply the patch package:
   ```bash
   java -jar morphe-cli.jar patch \
     -p patches/build/libs/patches-1.6.9.mpp \
     -o output-patched.apk \
     "target-input-app.apk"
   ```

---

## 📜 License & Section 7 Conditions

Licensed under the [GNU General Public License v3.0](LICENSE), with additional terms under GPLv3 Section 7:

- **Name Restriction (7c):** The name **"Morphe"** is a registered trademark/identity. You may not name derivative works "Morphe." Derivative forks must use a completely distinct brand name.

See the [LICENSE](LICENSE) file for the full terms and the [NOTICE](NOTICE) file for section 7 conditions.
