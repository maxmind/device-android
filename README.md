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
    implementation("com.maxmind.device:device-sdk:0.1.0-SNAPSHOT")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.maxmind.device:device-sdk:0.1.0-SNAPSHOT'
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

        val config = SdkConfig.Builder("your-api-key")
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
        .onSuccess {
            Log.d("SDK", "Data sent successfully")
        }
        .onFailure { error ->
            Log.e("SDK", "Failed to send data", error)
        }
}
```

#### Using Callbacks (Java-compatible)

```kotlin
DeviceTracker.getInstance().collectAndSend { result ->
    result.onSuccess {
        Log.d("SDK", "Data sent successfully")
    }.onFailure { error ->
        Log.e("SDK", "Failed to send data", error)
    }
}
```

#### Java Example

```java
DeviceTracker.getInstance().collectAndSend(result -> {
    if (result.isSuccess()) {
        Log.d("SDK", "Data sent successfully");
    } else {
        Throwable error = result.exceptionOrNull();
        Log.e("SDK", "Failed to send data", error);
    }
});
```

### 3. Manual Data Collection

Collect device data without sending:

```kotlin
val deviceData = DeviceTracker.getInstance().collectDeviceData()
println("Device: ${deviceData.manufacturer} ${deviceData.model}")
```

## Configuration Options

### SdkConfig.Builder

```kotlin
val config = SdkConfig.Builder("your-api-key")
    .serverUrl("https://custom-server.com/api")  // Optional: Custom server URL
    .enableLogging(true)                          // Optional: Enable debug logging
    .collectionInterval(60_000)                   // Optional: Auto-collect every 60 seconds
    .build()
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `apiKey` | String | *required* | Your MaxMind API key |
| `serverUrl` | String | `https://device-api.maxmind.com/v1` | MaxMind API endpoint |
| `enableLogging` | Boolean | `false` | Enable debug logging |
| `collectionIntervalMs` | Long | `0` | Auto-collection interval (0 = disabled) |

## Permissions

The SDK requires the following permissions (automatically included):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Collected Data

The SDK collects the following device information:

- **Device ID**: Optional unique identifier
- **Manufacturer**: Device manufacturer (e.g., "Samsung", "Google")
- **Model**: Device model name
- **Brand**: Device brand name
- **OS Version**: Android version
- **SDK Version**: Android SDK/API level
- **Screen Resolution**: Display resolution
- **Screen Density**: Display density
- **Timestamp**: Collection timestamp

All data collection respects user privacy and Android security policies.

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

### Code Quality Checks

```bash
# Run Detekt
./gradlew detekt

# Run ktlint
./gradlew ktlintCheck

# Auto-format with ktlint
./gradlew ktlintFormat
```

## Publishing

### Maven Central

The SDK is configured for Maven Central publishing:

```bash
./gradlew :device-sdk:publishReleasePublicationToMavenCentralRepository
```

Required credentials (set in `local.properties` or environment variables):

```properties
signing.keyId=YOUR_KEY_ID
signing.password=YOUR_KEY_PASSWORD
signing.secretKeyRingFile=/path/to/secring.gpg
mavenCentralUsername=YOUR_USERNAME
mavenCentralPassword=YOUR_PASSWORD
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and code quality checks
5. Submit a pull request

## License

This software is Copyright (c) 2025 by MaxMind, Inc.

This is free software, licensed under the
[Apache License, Version 2.0](LICENSE-APACHE) or the [MIT License](LICENSE-MIT),
at your option. Copyright 2025 MaxMind, Inc.

## Support

- Email: support@maxmind.com
- Issues: [GitHub Issues](https://github.com/maxmind/device-android/issues)
- Docs: [API Documentation](https://maxmind.github.io/device-android/)

## Changelog

### 0.1.0-SNAPSHOT (2025-10-28)

- Initial release
- Basic device data collection
- HTTP API integration
- Automatic collection intervals
- Java compatibility
