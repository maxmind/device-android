# Changelog

## 0.2.1 (TBD)

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
