plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.maxmind.device"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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

    kotlinOptions {
        jvmTarget = "17"

        // Enable explicit API mode for better library API design
        freeCompilerArgs +=
            listOf(
                "-Xexplicit-api=strict",
                "-opt-in=kotlin.RequiresOptIn",
            )
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
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

// Dokka configuration
tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

// Maven publishing configuration
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "device-sdk"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set(findProperty("POM_NAME")?.toString() ?: "MaxMind Device SDK")
                description.set(
                    findProperty("POM_DESCRIPTION")?.toString()
                        ?: "Android SDK for collecting and reporting device data to MaxMind",
                )
                url.set(findProperty("POM_URL")?.toString() ?: "")
                inceptionYear.set(findProperty("POM_INCEPTION_YEAR")?.toString() ?: "2025")

                licenses {
                    license {
                        name.set(findProperty("POM_LICENSE_NAME")?.toString() ?: "")
                        url.set(findProperty("POM_LICENSE_URL")?.toString() ?: "")
                        distribution.set(findProperty("POM_LICENSE_DIST")?.toString() ?: "")
                    }
                }

                developers {
                    developer {
                        id.set(findProperty("POM_DEVELOPER_ID")?.toString() ?: "")
                        name.set(findProperty("POM_DEVELOPER_NAME")?.toString() ?: "")
                        url.set(findProperty("POM_DEVELOPER_URL")?.toString() ?: "")
                    }
                }

                scm {
                    url.set(findProperty("POM_SCM_URL")?.toString() ?: "")
                    connection.set(findProperty("POM_SCM_CONNECTION")?.toString() ?: "")
                    developerConnection.set(
                        findProperty("POM_SCM_DEV_CONNECTION")?.toString() ?: ""
                    )
                }
            }
        }
    }

    repositories {
        maven {
            name = "mavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("mavenCentralUsername")?.toString() ?: ""
                password = findProperty("mavenCentralPassword")?.toString() ?: ""
            }
        }
    }
}

// Signing configuration
signing {
    if (findProperty("signing.keyId") != null) {
        sign(publishing.publications["release"])
    }
}

// Task to generate Javadoc from Dokka
tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.get().outputDirectory)
}
