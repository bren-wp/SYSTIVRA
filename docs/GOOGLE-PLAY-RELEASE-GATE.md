# SYSTIVRA — Google Play release gate (November 2026)

## Verified source configuration
- Android package: `com.systivra.app`
- Android target API: **36**, meets the Play requirement for new app submissions since August 31, 2026.
- Minimum API: **24**; test on Android 7.0 through Android 16 before publishing.
- Splash → Dashboard, no registration/login, English language and dark appearance.
- The Android manifest does **not** request `INTERNET`; it requests `ACCESS_NETWORK_STATE` and `VIBRATE` as normal permissions, and `PACKAGE_USAGE_STATS` for an optional special access feature.
- `android:allowBackup=false` for app-private SQLite monitoring recordings.

## Required reviews before external release
1. **No-crash gate:** run instrumented UI tests and stability/ANR scenarios on Android API 24, 29, 33, 35 and 36, foreground/background, denied Usage Access, missing sensors, app restarts and device rotation. A compilation success is not a crash-free guarantee.
2. **Screenshot gate:** obtain authentic in-app screenshots of the latest release, without System UI/Quickstep ANR dialogs. Google's screenshot guidelines prohibit misleading or concept-only imagery. At least two screenshots are mandatory; four 1080×1920 screenshots are recommended. Inspect images before submission.
3. **Assets:** `brending/google-play/assets/icon-512.png` (512x512 RGBA PNG <=1 MB) and `feature-graphic-1024x500.png` (1024x500 24-bit PNG). The eight concept images are intentionally under `concepts-NOT-FOR-PLAY` and must NOT be uploaded as screenshots.
4. **Privacy:** publish a stable HTTPS privacy policy explaining locally persisted device metrics, user-initiated CSV/JSON exports and optional Usage Access. Complete Data Safety truthfully according to the actual production implementation, not estimates.
5. **Signing:** only a private, authorized upload keystore may sign the production AAB. No keys/passwords in the repository or CI logs. Google Play App Signing setup must match the account.
6. **Permissions:** Usage Access is optional and user-enabled in Android Settings. Document why it is used and ensure the app remains usable when denied. Evaluate Google Play permissions/content policies for the account.
7. **Listing:** no fabricated device optimization, hardware health scores, inaccessible CPU/GPU metrics, crash-free guarantees, rankings or fake reviews.
8. **App Bundle:** validate signed AAB with bundletool/Play internal testing, test installation/update and crash/ANR reports before production rollout.
9. **Accessibility:** TalkBack labels, controls, touch targets, font scaling, screen orientation and contrast on different devices.
10. **Visual matching:** 1:1 claims require actual reference-vs-emulator diffs for each screen; two added screens have no approved baseline.

## Official documents
- [Play target API policy](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Google Play screenshot and artwork rules](https://support.google.com/googleplay/android-developer/answer/9866151)
- [Store metadata policy](https://support.google.com/googleplay/android-developer/answer/9898842)
- [Android app quality](https://developer.android.com/docs/quality-guidelines)

**Status:** Preview. This repository does not certify Play approval, stability across all devices or visual 1:1 compliance.

## November 15, 2026 dependency refresh

AGP upgraded from 9.3.0 to 9.4.0; AndroidX Core SplashScreen upgraded from 1.0.1 to 1.2.0. Other AndroidX/Kotlin libraries remain on published stable versions (not alpha/RC). Dependabot PRs will propose future upgrades after CI checks. The core Android package already targets API 36 and supports minSdk 24. The latest Play listing and screenshot rules still need verification in Play Console before submission; no screenshot is considered store-ready merely because it exists in a CI artifact.
