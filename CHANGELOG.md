# Changelog

## 0.2.0 (unreleased)

- **Breaking:** `collectAndSend()` and `sendDeviceData()` now return
  `Result<TrackingResult>` instead of `Result<Unit>`. The `TrackingResult`
  contains a `trackingToken` property for use with the minFraud API's
  `/device/tracking_token` field.

## 0.1.0 (2026-01-09)

- Initial release
