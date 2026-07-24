# Changelog

## 0.3.1 (TBD)

- Removed the unused `androidx.lifecycle:lifecycle-runtime-ktx` dependency. In
  0.3.0 this dependency was at version 2.11.0, which forced consumers with any
  `androidx.lifecycle` Compose artifacts on their classpath up to 2.11.0 via
  androidx version alignment, in turn requiring compileSdk 37 and a newer
  Android Gradle Plugin. The SDK never used this dependency, and removing it
  eliminates the toolchain requirement. Reported by Dzmitry Struk. GitHub #63.

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
