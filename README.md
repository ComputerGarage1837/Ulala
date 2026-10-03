# Loon & Hearth Staff Android App 1.0.0

Native Android staff app and a separate WooCommerce connector. The existing storefront theme and customer/payment plugin are unchanged.

## Included

- One-time 8–12 digit staff PIN login; encrypted device token held by Android Keystore. No PIN or WooCommerce consumer secret is stored on the phone.
- Orders with pagination, status filters, customer contact, items, fees, payment type and fulfilment method.
- Status changes with confirmation and stale-order checks.
- Partial or full amount refunds via the existing WooCommerce payment gateway when it supports automatic refunds. For cash/e-transfer, staff must return funds separately and confirm before recording the refund. No automatic restocking.
- Firebase new-order push notifications, private notification text, and tap to open the order.
- In-app GitHub update check, APK size/hash verification, exact package/version/signing-certificate checks, Android installation confirmation.
- WordPress device revocation and Firebase credential settings.

## What is not yet activated

This source project has not been built into an APK or deployed to the live shop. The Android SDK/Gradle tools were obtained, but the full Gradle build could not resolve the Android plugin through this environment’s Java network path. Android source type-checking against SDK 35 and actual Firebase/AndroidX classes passed; this does not substitute for an APK build/device test. GitHub's connected tools cannot create repositories. Firebase is not configured. Use the steps below to activate those parts; do not treat this source bundle as a verified production APK.

## 1. Install the connector

Upload `loon-hearth-staff-connector-v1.0.0.zip` through WordPress Plugins → Add New → Upload Plugin, and activate it with WooCommerce active. It supports WooCommerce HPOS.

Open WooCommerce → Staff Android App. Enter the numeric ID of an administrator or shop-manager user who can manage WooCommerce; set an 8–12 digit PIN. Share the PIN only with authorized staff. Every device using it has the chosen user's order/refund privileges. Devices are listed below the settings; Revoke blocks subsequent API access. Changing the PIN revokes all devices. HTTPS is required.

The API is `/wp-json/lh-staff/v1`. Exclude it from host/CDN caching. Ensure your hosting preserves the HTTP Authorization header. A response of “Please sign in” after login usually means that header is being stripped.

## 2. Create Firebase project

1. In Firebase Console create a project (suggested name `loon-hearth-staff`). Analytics is optional.
2. Add Android app with package name **ca.loonandhearth.staff**. Download `google-services.json`.
3. Enable Firebase Cloud Messaging HTTP v1 API.
4. In Project settings → Service accounts generate a private service-account key. Paste its JSON into the WordPress Staff Android App settings. This secret stays on the server. Never put it in the Android app, repository, or update release. Use a dedicated service account with Firebase Cloud Messaging API Admin permission where practical.
5. The Android Firebase configuration belongs at `android/app/google-services.json` for local builds, or in the GitHub secret below for Actions builds.
6. Use real server cron to run WordPress/Action Scheduler regularly (at least every minute). WP-Cron depending solely on visitor traffic can delay notifications. The plugin queues checkout orders through Action Scheduler where available, otherwise WP-Cron. It retries failed deliveries up to four times after the initial attempt, with per-device delivery tracking.

Notifications occur when a customer submits checkout, including orders awaiting e-transfer. Orders manually created by administrators are not included. Android notification permission, Google Play services, a working network and server cron are required. Push is not an offline guarantee. Revocation stops future server delivery; previously delivered generic notifications may remain on the phone until cleared.

## 3. Create GitHub repository and upload project

Repository: https://github.com/ComputerGarage1837/Ulala. The project uses this repository at the owner’s request. Source is at repository root.

Use a public release repository for this updater: release assets are public, but contain no credentials or shop/customer data. The app cannot download private GitHub releases and does not embed a GitHub token. If you want private source, keep source private and publish APK/update.json to a separate public release repository with a correspondingly adjusted workflow.

## Test APK

The Android test build workflow runs on every main-branch push and uploads `loon-hearth-staff-test-apk` as a GitHub Actions artifact. It compiles without Firebase credentials; push is disabled until the Android Firebase configuration is supplied. This debug APK is for staging tests and is not part of the release updater channel. Release signing differs, so uninstall the debug APK before installing the first production release.

## 4. Create permanent Android signing key

On a trusted computer with Java installed:

```sh
keytool -genkeypair -keystore staff-release.jks -alias staff -keyalg RSA -keysize 3072 -validity 10000
```

Back up this key and passwords securely. All updates must use the same signing key. Losing it means the old app cannot be updated in place. Do not commit the key to Git.

In GitHub repository Settings → Secrets and variables → Actions add secrets:

- `ANDROID_KEYSTORE_BASE64`: Base64 encoding of staff-release.jks
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`: `staff`
- `ANDROID_KEY_PASSWORD`
- `GOOGLE_SERVICES_JSON_BASE64`: Base64 encoding of google-services.json (Android client configuration, not service-account key)

Add repository variable `ANDROID_VERSION_CODE` with value `1`. Increment it for **every** later release. Keep the version tag consistent, e.g. v1.0.0, v1.0.1. The build must not reuse the same version code. GitHub Actions uses pinned Gradle/plugin versions and publishes a signed APK plus update.json. Actions runs compilation and Android lint; fix failures before deploying.

## 5. Build and release

Once pushed, tag the first release:

```sh
git tag v1.0.0
git push origin v1.0.0
```

The Android release workflow builds and signs the app, then creates a GitHub Release containing `loon-hearth-staff.apk` and `update.json`. The first APK can be downloaded directly from Releases and installed on the phone (allow installation from that download source). In the connector enter:

`https://github.com/ComputerGarage1837/Ulala/releases/latest/download/update.json`

Open the app, enter the shop URL and PIN. Allow notifications. Later updates are checked automatically at launch and through Settings → Check for updates. Android requires permission to install updates from this app, and installation confirmation; silent installation on ordinary consumer phones is not supported. Authentication persists through signed in-place updates.

For local Android Studio builds: open the `android` folder, use Java 17, SDK 35, Gradle 8.13. Debug builds are for development; they cannot replace an installed release build because signing differs.

## 6. Verify before live use

Use a staging store and a separate Firebase test project for initial checks:

1. Wrong PIN, sixth attempt throttled, correct PIN, app restart keeps login, revoked phone denied.
2. Order list, pagination, filters, customer/items/payment/fees match WooCommerce.
3. Change a status; check native emails and the site's referral rewards on completion. Test stale status conflicts.
4. Cash/e-transfer partial refund records exactly once after money returned. Check totals/refunded balance. Inventory stays unchanged.
5. Helcim test-mode refund goes through the gateway. This app does not bypass Helcim's WooCommerce plugin or implement its own payment API. If gateway refund support is absent, automatic refund is disabled.
6. Double submissions/timeouts: never blindly retry. A refund marked submitted/needs-review blocks further app refunds until an administrator reconciles it in WooCommerce/payment dashboard. The plugin also locks concurrent order mutations. If a PHP process dies while holding a lock, an administrator must investigate and remove the corresponding `lhs_lock_ORDERID` option only after reconciliation.
7. New checkout triggers push in foreground/background; tapping opens correct order. Verify denied notification permission and token refresh.
8. Release v1.0.1 with version code 2, same key. Verify update preserves login; wrong signature/hash/version must fail.

## Important refund accounting

Refunds in this app are amount-only; they do not allocate refunded quantities/tax per line item. For itemized tax/accounting refunds, use WooCommerce's backend. Full refunds use WooCommerce's normal order/refund behavior. Status selection does not mark money collected or charge a card; use existing payment workflow to reconcile cash/e-transfer payments. Staff should never mark an unpaid order refunded without actual return of funds.

## Data and recovery

No order data is persisted in app storage. Network requests use HTTPS and are not cached. Device tokens are hashed in the WordPress database and encrypted on the phone; Firebase tokens are held server-side for delivery. PIN hash and Firebase credentials are WordPress options; restrict database/backup access. The app disables Android backup. Changing WordPress staff-user permissions revokes access at the next request.

Backend troubleshooting: check WordPress/WooCommerce logs, server Authorization forwarding, REST caching, and scheduled action queues. No real refund or order status was changed while creating this package.
