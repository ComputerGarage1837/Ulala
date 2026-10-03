# Validation status — 1.0.0

Passed locally:
- PHP 8 syntax check for connector.
- 12 focused refund/status guard cases: over-balance, negative/over-precision amount, missing reason, missing manual money-return confirmation, invalid mode, repeated request, prior uncertain/submitted request, inappropriate manual payment method, direct refunded status bypass, stale status.
- All Android Java source type-checked using Eclipse ECJ Java 17 against Android SDK 35, actual AndroidX Core 1.15.0, Firebase Messaging 24.1.1 and Play Services Tasks 18.2.0. R/BuildConfig were temporary generated compile symbols. This is not an APK build.
- XML resources/manifest parsed and release manifest generator checked.
- ZIP integrity checked before delivery.

Not yet passed:
- Full Gradle APK build and Android lint: plugin resolution failed through local Java networking.
- Physical-device installation/UI/session/notification tests.
- Actual WooCommerce staging integration with database locking, stock, emails/referrals and gateway test refunds.
- Firebase credentials/push delivery.
- GitHub push, Actions build, signing and in-app update test.

No live orders, payments, refunds or website settings were altered. No production signing key, PIN, Firebase secret or GitHub token is included.
