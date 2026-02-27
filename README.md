# MaxMind Device SDK for Android

Android SDK for collecting and reporting device data to MaxMind.

## Requirements

- Android API 29+ (Android 10+)
- Kotlin 1.9.22+
- AndroidX libraries

## Installation

### Gradle (Kotlin DSL)

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.maxmind.device:device-sdk:0.1.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.maxmind.device:device-sdk:0.1.0'
}
```

## Quick Start

### 1. Initialize the SDK

Initialize the SDK in your `Application` class or main activity:

```kotlin
import com.maxmind.device.DeviceTracker
import com.maxmind.device.config.SdkConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = SdkConfig.Builder(123456)  // Your MaxMind account ID
            .enableLogging(BuildConfig.DEBUG)
            .build()

        DeviceTracker.initialize(this, config)
    }
}
```

### 2. Collect and Send Device Data

#### Using Kotlin Coroutines

```kotlin
lifecycleScope.launch {
    DeviceTracker.getInstance().collectAndSend()
        .onSuccess { trackingResult ->
            // Pass the token to your backend for minFraud integration
            sendToBackend(trackingResult.trackingToken)
        }
        .onFailure { error ->
            Log.e("SDK", "Failed to send data", error)
        }
}
```

#### Using Callbacks (Java-compatible)

```kotlin
DeviceTracker.getInstance().collectAndSend { result ->
    result.onSuccess { trackingResult ->
        sendToBackend(trackingResult.trackingToken)
    }.onFailure { error ->
        Log.e("SDK", "Failed to send data", error)
    }
}
```

#### Java Example

Kotlin's `Result<T>` is a value class with limited Java interop. If you need the
tracking token in Java, add a thin Kotlin bridge to your project:

```kotlin
// In your project (e.g., DeviceTrackerBridge.kt)
fun collectAndSend(
    tracker: DeviceTracker,
    onSuccess: java.util.function.Consumer<String>,
    onFailure: java.util.function.Consumer<Throwable>,
) {
    tracker.collectAndSend { result ->
        result.onSuccess { onSuccess.accept(it.trackingToken) }
            .onFailure { onFailure.accept(it) }
    }
}
```

```java
// Java caller
collectAndSend(
    DeviceTracker.getInstance(),
    token -> sendToBackend(token),
    error -> Log.e("SDK", "Failed to send data", error)
);
```

### 3. Linking Device Data to minFraud Transactions

After collecting and sending device data, pass the tracking token to the
minFraud API to link device data with transactions:

```kotlin
lifecycleScope.launch {
    DeviceTracker.getInstance().collectAndSend()
        .onSuccess { trackingResult ->
            // Pass trackingResult.trackingToken to your backend,
            // then include it in the minFraud request's
            // /device/tracking_token field
            sendToBackend(trackingResult.trackingToken)
        }
}
```

## Configuration Options

### SdkConfig.Builder

```kotlin
val config = SdkConfig.Builder(123456)              // Your MaxMind account ID
    .serverUrl("https://custom-server.com/api")  // Optional: Custom server URL
    .enableLogging(true)                          // Optional: Enable debug logging
    .collectionInterval(60_000)                   // Optional: Auto-collect every 60 seconds
    .build()
```

| Builder Method            | Type    | Default         | Description                                             |
| ------------------------- | ------- | --------------- | ------------------------------------------------------- |
| `Builder(accountID)`      | Int     | _required_      | Your MaxMind account ID                                 |
| `.serverUrl(url)`         | String  | Default servers | Custom server URL                                       |
| `.enableLogging(enabled)` | Boolean | `false`         | Enable debug logging                                    |
| `.collectionInterval(ms)` | Long    | `0`             | Auto-collection interval in milliseconds (0 = disabled) |

## Permissions

The SDK requires the following permissions (automatically included):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## ProGuard / R8

The SDK includes consumer ProGuard rules. No additional configuration is needed.

## Sample App

See the `sample` module for a complete working example demonstrating:

- SDK initialization
- Device data collection
- Data transmission
- Error handling

To run the sample app:

```bash
./gradlew :sample:installDebug
```

## Building the SDK

### Build Library

```bash
./gradlew :device-sdk:assemble
```

### Run Tests

```bash
./gradlew :device-sdk:test
```

### Generate Documentation

```bash
./gradlew :device-sdk:dokkaHtml
```

Documentation will be generated in `device-sdk/build/dokka/`.

## Contributing

1. Fork the repository
2. Set up your development environment (see [SETUP.md](SETUP.md))
3. Create a feature branch
4. Make your changes
5. Run tests and code quality checks
6. Submit a pull request

## License

This software is Copyright (c) 2025 by MaxMind, Inc.

This is free software, licensed under the
[Apache License, Version 2.0](LICENSE-APACHE) or the [MIT License](LICENSE-MIT),
at your option. Copyright 2025 MaxMind, Inc.

## Support

For support, please visit
[maxmind.com/en/company/contact-us](https://www.maxmind.com/en/company/contact-us).

If you find a bug or have a feature request, please open an issue on
[GitHub](https://github.com/maxmind/device-android/issues).
