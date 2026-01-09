# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with
code in this repository.

## Project Overview

This is an Android SDK library for collecting device data and sending it to
MaxMind servers. The project uses Kotlin with Java compatibility (@JvmStatic,
@JvmOverloads) and is designed to be published to Maven Central.

**Key Design Principles:**

- Kotlin-first with explicit API mode (`-Xexplicit-api=strict`)
- Java compatibility for broader adoption
- Coroutine-based async operations with callback alternatives
- Singleton pattern with initialization guard
- Builder pattern for configuration

## Naming Conventions

Follow Kotlin conventions (kotlinlang.org/docs/coding-conventions.html):

- 2-letter acronyms: ALL CAPS (`ID`, `OS`)
- 3+ letter acronyms: First letter only (`Gpu`, `Drm`, `Api`, `Sdk`, `Cpu`,
  `Dpi`)

## Build Commands

### Core Development

```bash
# Build SDK library only (fastest for SDK development)
./gradlew :device-sdk:assemble

# Build debug variants (skips minification issues)
./gradlew assembleDebug

# Build SDK library with all variants
./gradlew :device-sdk:build

# Install sample app to connected device
./gradlew :sample:installDebug
```

### Testing

```bash
# Run unit tests for SDK
./gradlew :device-sdk:test

# Run specific test class
./gradlew :device-sdk:test --tests "com.maxmind.device.DeviceTrackerTest"

# Run tests with coverage (JaCoCo)
./gradlew :device-sdk:testDebugUnitTest jacocoTestReport
```

### Code Quality

```bash
# Run all quality checks
./gradlew detekt ktlintCheck

# Auto-fix formatting issues
./gradlew ktlintFormat

# Generate API documentation
./gradlew :device-sdk:dokkaHtml
# Output: device-sdk/build/dokka/
```

### Pre-commit Formatting

This project uses [precious](https://github.com/houseabsolute/precious) for
pre-commit hooks. Before committing, run:

```bash
# Tidy all staged files (fixes formatting issues)
precious tidy -g

# Then stage the tidied files and commit
git add -u && git commit
```

If a commit fails due to formatting, run `precious tidy -g` and retry.

### Publishing

See `README.dev.md` for the full release process. For manual publishing:

```bash
# Publish to Maven Central via Central Portal (requires credentials in local.properties)
./gradlew :device-sdk:publishAndReleaseToMavenCentral
```

## Architecture

### SDK Entry Point Pattern

The SDK uses a **singleton pattern with initialization guard**:

1. **DeviceTracker** - Main singleton entry point
   - Private constructor prevents direct instantiation
   - `initialize(Context, SdkConfig)` must be called first
   - `getInstance()` returns the initialized instance or throws
   - `isInitialized()` checks initialization state

2. **Lifecycle Management**
   - Stores `applicationContext` (not activity context)
   - Creates coroutine scope with `SupervisorJob + Dispatchers.IO`
   - `shutdown()` cancels scope and closes HTTP client
   - Automatic collection runs in background if `collectionIntervalMs > 0`

### Component Architecture

**Four-layer architecture:**

1. **Public API Layer** (`DeviceTracker.kt`)
   - Singleton facade pattern
   - Both suspend functions and callback-based methods
   - Example: `collectAndSend()` (suspend) and `collectAndSend(callback)`
     (callbacks)

2. **Configuration Layer** (`config/SdkConfig.kt`)
   - Immutable configuration with builder pattern
   - `SdkConfig.Builder` validates inputs in `build()`
   - Default servers: `d-ipv6.mmapiws.com` and `d-ipv4.mmapiws.com`
     (dual-request flow)

3. **Data Collection Layer** (`collector/DeviceDataCollector.kt`)
   - Collects device information via Android APIs
   - Uses `WindowManager` for display metrics
   - Returns `DeviceData` serializable model

4. **Network Layer** (`network/DeviceApiClient.kt`)
   - Ktor HTTP client with Android engine
   - kotlinx.serialization for JSON
   - Optional logging based on `enableLogging` config
   - Returns `Result<ServerResponse>` for error handling

   **Dual-Request Flow (IPv6/IPv4):** To capture both IP addresses for a device,
   the SDK uses a dual-request flow:
   1. First request sent to `d-ipv6.mmapiws.com/device/android`
   2. If response contains `ip_version: 6`, a second request is sent to
      `d-ipv4.mmapiws.com/device/android`
   3. The IPv4 request is fire-and-forget (failures don't affect the result)
   4. The `stored_id` from the IPv6 response is returned and persisted

   If a custom server URL is configured via `SdkConfig.Builder.serverUrl()`, the
   dual-request flow is disabled and only a single request is sent.

### Data Model

**DeviceData** (`model/DeviceData.kt`):

- Marked with `@Serializable` for kotlinx.serialization
- All fields are public for Java compatibility
- Immutable data class
- Optional `deviceId` field (can be null)

## Java Compatibility Strategy

When adding new public APIs:

1. **Use @JvmStatic for static/companion methods**

   ```kotlin
   companion object {
       @JvmStatic
       fun initialize(context: Context, config: SdkConfig): DeviceTracker
   }
   ```

2. **Use @JvmOverloads for optional parameters**

   ```kotlin
   @JvmOverloads
   public fun collectAndSend(callback: ((Result<Unit>) -> Unit)? = null)
   ```

3. **Provide callback-based alternatives to suspend functions**

   ```kotlin
   // Suspend function for Kotlin
   suspend fun collectAndSend(): Result<Unit>

   // Callback version for Java
   fun collectAndSend(callback: (Result<Unit>) -> Unit)
   ```

4. **Use explicit visibility modifiers**
   - All public APIs must have `public` keyword (enforced by
     `-Xexplicit-api=strict`)

## Dependency Management

All dependencies are centralized in `gradle/libs.versions.toml`:

**Key Dependencies:**

- Ktor 2.3.7 (HTTP client with Android engine)
- kotlinx.serialization 1.6.2 (JSON serialization)
- kotlinx.coroutines 1.7.3 (async operations)
- Detekt 1.23.5 (Kotlin linting)
- ktlint 12.1.0 (code formatting)
- Dokka 1.9.10 (API documentation)

**To update a dependency:**

1. Edit version in `gradle/libs.versions.toml`
2. Sync Gradle
3. Run `./gradlew :device-sdk:build` to verify

## ProGuard/R8 Configuration

The SDK includes consumer ProGuard rules in `consumer-rules.pro`:

- Keeps public SDK API
- Keeps kotlinx.serialization classes
- Keeps Ktor classes
- Apps using this SDK automatically inherit these rules

## Environment Setup

**Required:**

1. Java 21 (Android Studio JDK) configured in `gradle.properties`:

   ```
   org.gradle.java.home=/home/greg/.local/share/android-studio/jbr
   ```

2. Android SDK with API 34 at `~/Android/Sdk`

3. `local.properties` file (gitignored):
   ```properties
   sdk.dir=/home/greg/Android/Sdk
   ```

**Java Version Issues:**

- The project requires Java 17+ (set to use Java 21 from Android Studio)
- If you see "25" error, Java 25 is being used instead
- Fix by setting `org.gradle.java.home` in `gradle.properties`

## Common Issues

### Build Failures

**"SDK licenses not accepted"**

```bash
~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --licenses
```

**"Resource mipmap/ic_launcher not found"**

- Sample app has no launcher icons (intentional for simplicity)
- Build works for `assembleDebug`, may fail on `assemble` (release variant)

**Detekt/ktlint failures**

- Skip with: `./gradlew build -x detekt -x ktlintCheck`
- Auto-fix formatting: `./gradlew ktlintFormat`

### Release Build MinifyEnabled

The sample app has `isMinifyEnabled = true` for release builds, which may cause
R8 issues. For development:

```bash
# Build debug variant instead
./gradlew :sample:assembleDebug
```

## Testing Strategy

- **Unit tests** in `device-sdk/src/test/` use JUnit 5, MockK, and Robolectric
- **Android instrumented tests** in `device-sdk/src/androidTest/`
- Test coverage with JaCoCo
- Turbine for testing Flows/coroutines

When adding features, write unit tests that:

- Mock the Android Context with MockK or use Robolectric
- Test both success and failure paths
- Test Java compatibility if API is public

## Version Catalog Structure

Uses Gradle version catalog in `gradle/libs.versions.toml`:

- `[versions]` - Version numbers
- `[libraries]` - Individual dependencies
- `[plugins]` - Gradle plugins
- `[bundles]` - Grouped dependencies (e.g., `ktor`, `testing`)

Access in build files: `libs.ktor.client.core`, `libs.plugins.kotlin.android`

## Maven Publishing Configuration

Publishing uses the
[Vanniktech Maven Publish](https://github.com/vanniktech/gradle-maven-publish-plugin)
plugin configured in `device-sdk/build.gradle.kts`. The plugin publishes to
Maven Central via Central Portal with automatic release.

Credentials are read from `~/.m2/settings.xml` (server id `central`) to share
credentials with other MaxMind Maven projects. GPG signing uses the system `gpg`
command, so existing `~/.gnupg` configuration is used automatically.

See `README.dev.md` for the full release process and credential setup.

## Module Structure

- `device-sdk/` - Android library module (the SDK)
- `sample/` - Android application module (demo app)
- `config/detekt/` - Shared Detekt configuration
- `gradle/` - Gradle wrapper and version catalog

Both modules are independent but `sample` depends on `device-sdk` via
`implementation(project(":device-sdk"))`.
