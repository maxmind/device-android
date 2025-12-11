import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

// Read local.properties for debug configuration
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val debugServerUrl = localProperties.getProperty("debug.server.url", "")
val debugCaCertPath = localProperties.getProperty("debug.ca.cert", "")
val accountId = localProperties.getProperty("maxmind.account.id", "0")

android {
    namespace = "com.maxmind.device.sample"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.maxmind.device.sample"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEBUG_SERVER_URL", "\"$debugServerUrl\"")
        buildConfigField("int", "MAXMIND_ACCOUNT_ID", accountId)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // SDK module
    implementation(project(":device-sdk"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Material Design
    implementation(libs.material)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
}

// Detekt configuration
detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

// Debug CA certificate support for local development
// Usage: Add to local.properties:
//   debug.ca.cert=/path/to/your/ca.crt
//   debug.server.url=https://localhost:8443
val hasDebugCert = debugCaCertPath.isNotEmpty() && file(debugCaCertPath).exists()

if (hasDebugCert) {
    // Copy certificate to res/raw
    tasks.register<Copy>("copyDebugCaCert") {
        from(debugCaCertPath)
        into("src/main/res/raw")
        rename { "debug_ca.crt" }
    }

    // Generate network security config that includes the bundled cert
    tasks.register("generateNetworkSecurityConfig") {
        dependsOn("copyDebugCaCert")
        val outputFile = file("src/main/res/xml/network_security_config.xml")
        outputs.file(outputFile)
        doLast {
            outputFile.parentFile.mkdirs()
            outputFile.writeText(
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<!-- Generated - do not edit. Configure via local.properties -->
                |<network-security-config>
                |    <debug-overrides>
                |        <trust-anchors>
                |            <certificates src="@raw/debug_ca" />
                |            <certificates src="user" />
                |            <certificates src="system" />
                |        </trust-anchors>
                |    </debug-overrides>
                |    <domain-config>
                |        <domain includeSubdomains="false">localhost</domain>
                |        <trust-anchors>
                |            <certificates src="@raw/debug_ca" />
                |            <certificates src="user" />
                |            <certificates src="system" />
                |        </trust-anchors>
                |    </domain-config>
                |</network-security-config>
                """.trimMargin(),
            )
        }
    }

    tasks.named("preBuild") {
        dependsOn("generateNetworkSecurityConfig")
    }

    // Clean up generated files
    tasks.named("clean") {
        doLast {
            delete("src/main/res/raw/debug_ca.crt")
        }
    }
} else if (debugCaCertPath.isNotEmpty()) {
    logger.warn("Debug CA certificate not found at: $debugCaCertPath")
}
