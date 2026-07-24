# Changelog

## 0.4.0 (TBD)

- The published AAR now declares a `minCompileSdk` of 30. Consumers that minify,
  or that set a Java 9 or later `sourceCompatibility`, already needed
  `compileSdk` 30: `InstallationInfoHelper` references
  `android.content.pm.InstallSourceInfo`, added in API 30, so R8 reported it as
  a missing class, and the Android Gradle plugin separately refuses to configure
  Java 9 or later compilation below 30. For consumers that do neither, this is a
  new requirement rather than a pre-existing one. The API-30 call is guarded by
  an `SDK_INT` check and is safe at runtime, so the floor trades an obscure R8
  failure for a clear Gradle error. If your build fails with a `minCompileSdk`
  error, raise `compileSdk` to 30 or later, or stay on 0.3.1. Note this is your
  app's `compileSdk` only: the SDK still supports API 27 and later devices, and
  `minSdk` is unchanged.
- The SDK is now built with Gradle 9.6.1 and the Android Gradle plugin 9.3.1.
  The public API and the published ABI are unchanged, and the artifact still
  imposes no Android Gradle plugin floor on consumers, so the toolchain change
  itself requires no action from consumers.

## 0.3.1 (2026-07-24)

- Removed the unused `androidx.lifecycle:lifecycle-runtime-ktx` dependency. In
  0.3.0 this dependency was at version 2.11.0, which forced consumers with any
  `androidx.lifecycle` Compose artifacts on their classpath up to 2.11.0 via
  androidx version alignment, in turn requiring compileSdk 37 and a newer
  Android Gradle Plugin. The SDK never used this dependency, and removing it
  eliminates the toolchain requirement. Reported by Dzmitry Struk. GitHub #63.
- Removed the unused `kotlinx-coroutines-android` dependency. The SDK only uses
  `Dispatchers.IO`, so it does not need the Android `Dispatchers.Main`
  integration this artifact provides.
- Removed the `androidx.core:core-ktx` dependency. Its only use was the
  `SharedPreferences.edit {}` extension, which has been replaced with the
  equivalent framework calls. The published artifact now has no `androidx`
  dependencies, so it no longer participates in androidx version alignment and
  imposes no `compileSdk` or Android Gradle Plugin floor on consumers.
  Previously `core-ktx` 1.18.0 required `compileSdk` 36 and AGP 8.9.1.

## 0.3.0 (2026-06-23)

- Lowered the minimum supported Android API level from 29 (Android 10) to 27
  (Android 8.1). Device data collection on API 27 and 28 falls back to
  pre-API-28 methods for the app version code and MediaDRM cleanup; no collected
  signals are lost.
- Fixed `enableLogging` not being forwarded from `SdkConfig` to
  `DeviceDataCollector`, which caused collector-level error logs to be silently
  suppressed even when logging was explicitly enabled.

## 0.2.0 (2026-02-27)

- **Breaking:** `collectAndSend()` now returns `Result<TrackingResult>` instead
  of `Result<Unit>`. The `TrackingResult` contains a `trackingToken` property
  for use with the minFraud API's `/device/tracking_token` field.
- **Breaking:** `collectDeviceData()` and `sendDeviceData()` are no longer part
  of the public API. Use `collectAndSend()` instead.

## 0.1.0 (2026-01-09)

- Initial release
