import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
    signing
    id("tech.apter.junit5.jupiter.robolectric-extension-gradle-plugin") version "0.9.0"
    id("me.champeau.gradle.japicmp") version "0.4.5"
}

android {
    namespace = "com.maxmind.device"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Build config fields for SDK metadata
        buildConfigField("String", "SDK_VERSION", "\"${project.version}\"")
        buildConfigField("String", "SDK_NAME", "\"MaxMind Device SDK\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)

        // Enable explicit API mode for better library API design
        freeCompilerArgs.addAll(
            "-Xexplicit-api=strict",
            "-opt-in=kotlin.RequiresOptIn",
        )
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Networking
    implementation(libs.bundles.ktor)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.ktor.client.mock)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.android.testing)

    // Android Testing
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.mockk.android)
}

// Enable JUnit 5 test discovery
tasks.withType<Test> {
    useJUnitPlatform()
}

// Detekt configuration
detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

// Dokka configuration (V2 API)
dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }
}

// Maven Central publishing configuration (using Vanniktech plugin)
// Only configure Maven Central upload when real credentials are available.
// Credentials come from env vars (set by release.sh from ~/.m2/settings.xml)
// or can be provided directly via ORG_GRADLE_PROJECT_mavenCentralUsername
val mavenCentralUsername = providers.gradleProperty("mavenCentralUsername").orNull ?: ""
val hasMavenCentralCredentials = mavenCentralUsername.isNotEmpty()

mavenPublishing {
    if (hasMavenCentralCredentials) {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    }
    signAllPublications()

    coordinates(
        groupId = "com.maxmind.device",
        artifactId = "device-sdk",
        version = project.version.toString(),
    )

    pom {
        name.set("MaxMind Device SDK")
        description.set("Android SDK for collecting and reporting device data to MaxMind")
        inceptionYear.set("2025")
        url.set("https://github.com/maxmind/device-android")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("maxmind")
                name.set("MaxMind, Inc.")
                url.set("https://www.maxmind.com/")
            }
        }

        scm {
            url.set("https://github.com/maxmind/device-android")
            connection.set("scm:git:git://github.com/maxmind/device-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/maxmind/device-android.git")
        }
    }
}

// Configure signing to use GPG command (like Maven) instead of in-memory keys
// This respects ~/.gnupg configuration and gpg-agent
signing {
    useGpgCmd()
}

// API compatibility checking with japicmp
// Compares the current build against the latest released version on Maven Central
// Update this version after each release (the release script should do this automatically)
val baselineVersion = "0.1.0"

// Download baseline AAR directly from Maven Central to avoid local project resolution
val downloadBaselineAar by tasks.registering {
    val outputFile = layout.buildDirectory.file("japicmp/baseline.aar")
    outputs.file(outputFile)
    doLast {
        val url = "https://repo1.maven.org/maven2/com/maxmind/device/device-sdk/$baselineVersion/device-sdk-$baselineVersion.aar"
        val destFile = outputFile.get().asFile
        destFile.parentFile.mkdirs()
        URI(url).toURL().openStream().use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        logger.lifecycle("Downloaded baseline AAR from $url")
    }
}

// Extract classes.jar from baseline AAR for comparison
val extractBaselineClasses by tasks.registering(Copy::class) {
    dependsOn(downloadBaselineAar)
    from(zipTree(layout.buildDirectory.file("japicmp/baseline.aar"))) {
        include("classes.jar")
    }
    into(layout.buildDirectory.dir("japicmp/baseline"))
}

// Extract classes.jar from current AAR for comparison
val extractCurrentClasses by tasks.registering(Copy::class) {
    dependsOn("bundleReleaseAar")
    from(zipTree(layout.buildDirectory.file("outputs/aar/device-sdk-release.aar"))) {
        include("classes.jar")
    }
    into(layout.buildDirectory.dir("japicmp/current"))
}

tasks.register<me.champeau.gradle.japicmp.JapicmpTask>("japicmp") {
    dependsOn(extractBaselineClasses, extractCurrentClasses)
    oldClasspath.from(layout.buildDirectory.file("japicmp/baseline/classes.jar"))
    newClasspath.from(layout.buildDirectory.file("japicmp/current/classes.jar"))
    oldArchives.from(layout.buildDirectory.file("japicmp/baseline/classes.jar"))
    newArchives.from(layout.buildDirectory.file("japicmp/current/classes.jar"))
    accessModifier.set("public")
    onlyModified.set(true)
    failOnModification.set(true)
    includeSynthetic.set(false)
    ignoreMissingClasses.set(true)
    txtOutputFile.set(layout.buildDirectory.file("japicmp/report.txt"))
    htmlOutputFile.set(layout.buildDirectory.file("japicmp/report.html"))
}
