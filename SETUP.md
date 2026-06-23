# Project Setup Guide

## Prerequisites

- Java (the required version is pinned in `mise.toml`)
- Android SDK (install the platform and build-tools matching the `compileSdk` in
  `gradle/libs.versions.toml`)
- Gradle (included via the wrapper)

## Initial Setup

### 1. Install Android SDK

#### Option A: Using mise (Recommended)

[mise](https://mise.jdx.dev/) reads `mise.toml` and installs the toolchain at
the versions this project expects, so you don't have to track them by hand:

```bash
mise install      # Installs Java, the Android SDK command-line tools, etc.
mise run setup    # Accepts licenses, installs platform packages, writes local.properties
```

This also creates `local.properties`, so you can skip step 2 below.

#### Option B: Using Android Studio

1. Open Android Studio
2. Navigate to **Tools → SDK Manager**
3. Install the platform and build-tools matching the `compileSdk` in
   `gradle/libs.versions.toml`, plus the Command-line Tools and Platform-Tools
4. Accept all licenses when prompted
5. Note the SDK location path (shown at top of SDK Manager)

#### Option C: Command Line

```bash
# Download the latest command-line tools ("Command line tools only") from
# https://developer.android.com/studio and unpack them:
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
unzip ~/Downloads/commandlinetools-linux-*.zip
mv cmdline-tools latest

# Accept licenses, then install the platform and build-tools for the project's
# compileSdk (see gradle/libs.versions.toml). For example, if compileSdk = 36:
~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --licenses
~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

### 2. Configure SDK Location

Create a `local.properties` file in the project root:

```properties
sdk.dir=/path/to/your/Android/Sdk
```

Replace `/path/to/your/Android/Sdk` with your actual SDK location:

- Default on Linux: `~/Android/Sdk` or `/home/yourusername/Android/Sdk`
- From Android Studio: Check **File → Settings → Appearance & Behavior → System
  Settings → Android SDK**

### 3. Build the Project

```bash
./gradlew build
```

## Common Build Commands

```bash
# Build everything
./gradlew build

# Build SDK library only
./gradlew :device-sdk:assemble

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Run code quality checks
./gradlew detekt ktlintCheck

# Format code with ktlint
./gradlew ktlintFormat

# Install sample app to device
./gradlew :sample:installDebug

# Generate documentation
./gradlew dokkaHtml
```

## Publishing to Maven Central

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

## Environment Variables

Set these in your shell profile (`~/.bashrc` or `~/.zshrc`):

```bash
# Android SDK
export ANDROID_HOME=~/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

# Java (if using Android Studio's JDK)
export JAVA_HOME=~/.local/share/android-studio/jbr
export PATH=$JAVA_HOME/bin:$PATH
```

## Troubleshooting

### License Not Accepted

```bash
# Accept all licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
```

### Wrong Java Version

```bash
# Use Android Studio's JDK
export JAVA_HOME=~/.local/share/android-studio/jbr
./gradlew build
```

### SDK Not Found

Ensure `local.properties` exists with correct SDK path:

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

### Gradle Daemon Issues

```bash
# Stop all Gradle daemons
./gradlew --stop

# Clean and rebuild
./gradlew clean build
```

## IDE Setup

### Android Studio

1. Open Android Studio
2. **File → Open** → Select the project directory
3. Android Studio will automatically detect the Gradle project
4. Wait for Gradle sync to complete
5. You're ready to develop!

### IntelliJ IDEA

1. Open IntelliJ IDEA
2. **File → Open** → Select the project directory
3. Wait for Gradle sync
4. Install the Android plugin if prompted

### VS Code

Install extensions:

- Kotlin Language
- Gradle for Java
- Android iOS Emulator

## Next Steps

1. Review the [README.md](README.md) for API documentation
2. Check out the sample app in `/sample`
3. Run the tests: `./gradlew test`
4. Start developing!

## Project Structure

```
device-android/
├── device-sdk/          # Main SDK library
│   ├── src/
│   │   ├── main/        # SDK source code
│   │   └── test/        # Unit tests
│   └── build.gradle.kts
├── sample/              # Sample application
│   ├── src/
│   │   └── main/        # Sample app code
│   └── build.gradle.kts
├── config/              # Configuration files
│   └── detekt/          # Detekt rules
├── gradle/              # Gradle wrapper and version catalog
├── build.gradle.kts     # Root build file
├── settings.gradle.kts  # Project settings
└── README.md            # Main documentation
```

## Sample App Configuration

The sample app requires configuration via `local.properties` (gitignored).

### Required Configuration

Add your MaxMind account ID to `local.properties`:

```properties
maxmind.account.id=123456
```

Without this, the sample app will show an error when you try to initialize the
SDK.

### Local Development Server

The sample app can optionally connect to a local development server instead of
the production MaxMind servers.

#### Quick Start

1. **Add to `local.properties`**:

   ```properties
   maxmind.account.id=123456
   debug.server.url=https://localhost:8443
   debug.ca.cert=/path/to/your/ca.crt
   ```

2. **Set up ADB reverse port forwarding**:

   ```bash
   adb reverse tcp:8443 tcp:8443
   ```

3. **Build and install**:

   ```bash
   ./gradlew :sample:installDebug
   ```

### Configuration Options

Add these to `local.properties` (gitignored):

| Property             | Required | Description                                  |
| -------------------- | -------- | -------------------------------------------- |
| `maxmind.account.id` | Yes      | Your MaxMind account ID                      |
| `debug.server.url`   | No       | Server URL (e.g., `https://localhost:8443`)  |
| `debug.ca.cert`      | No       | Path to CA certificate for self-signed HTTPS |

Without the optional debug properties, the sample app connects to production
MaxMind servers.

### ADB Reverse Port Forwarding

When running the sample app on a physical device, `localhost` on the device
refers to the device itself, not your development machine. ADB reverse creates a
tunnel:

```bash
# Forward device's localhost:8443 to your machine's localhost:8443
adb reverse tcp:8443 tcp:8443

# Verify the forwarding is active
adb reverse --list

# Remove forwarding when done
adb reverse --remove tcp:8443
```

**Note:** You must re-run `adb reverse` each time you reconnect the device.

For emulators, you can alternatively use `10.0.2.2` which automatically routes
to the host machine's localhost.

### How Certificate Bundling Works

When `debug.ca.cert` is configured and the file exists, the build system:

1. Copies the certificate to `sample/src/main/res/raw/debug_ca.crt` (gitignored)
2. Generates a `network_security_config.xml` that trusts the bundled certificate
3. Sets `BuildConfig.DEBUG_SERVER_URL` for the app to use

When not configured, the sample app behaves normally with the default network
security config.

### Troubleshooting

**"Trust anchor for certification path not found"**

- Ensure `debug.ca.cert` points to a valid CA certificate file (not a leaf cert)
- Rebuild: `./gradlew :sample:clean :sample:installDebug`
- Verify the certificate was copied: `ls sample/src/main/res/raw/`

**Connection refused / timeout**

- Verify ADB reverse is active: `adb reverse --list`
- Ensure your local server is running on the correct port
- Check that the server binds to `0.0.0.0` or `localhost`

**Changes not taking effect**

- Run a clean build: `./gradlew :sample:clean :sample:installDebug`
- The network security config is generated at build time based on
  `local.properties`

## Support

If you encounter issues:

1. Check that all prerequisites are installed
2. Verify SDK location in `local.properties`
3. Run `./gradlew clean build --stacktrace` for detailed errors
4. Check GitHub issues or create a new one
