// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dependency.updates)
}

allprojects {
    group = "com.maxmind.device"
    version = "0.3.1"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// gradle-wrapper.properties is generated, so any hand edit there is lost the next
// time someone runs `./gradlew wrapper`. Declaring the values here means a
// regeneration reproduces them. distributionSha256Sum is deliberately not set
// here: it changes with every Gradle version, and Gradle already refuses to
// regenerate the wrapper for a new version while it is present unless
// --gradle-distribution-sha256-sum is passed. See README.dev.md.
tasks.withType<Wrapper>().configureEach {
    networkTimeout = 10000
    retries = 2
    retryBackOffMs = 500
}
