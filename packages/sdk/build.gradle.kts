import ai.anam.lab.client.gradle.Versions
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit")
    id("org.jetbrains.kotlin.native.cocoapods")
    id("org.jetbrains.dokka")
    id("ai.anam.lab.client.licensee")
    id("com.github.gmazzo.buildconfig")
    id("com.vanniktech.maven.publish")
}

kotlin {
    explicitApi()

    compilerOptions {
        // Automatically opt-in to our own (unsafe) APIs.
        freeCompilerArgs.add("-opt-in=ai.anam.lab.utils.UnsafeAnamApi")

        // Opt-in to ExperimentalTime for internal use only
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }

    cocoapods {
        version = "1.0.0"
        summary = "Shared module"
        homepage = "not published"
        ios.deploymentTarget = "13.0"
        name = "shared"

        // Version defined in consumer's Podfile.
        pod("WebRTC-SDK") {
            moduleName = "WebRTC"
        }

        podfile = project.file("../../apps/ios/Podfile")

        framework {
            baseName = "shared"
            isStatic = true
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.ws)
                implementation(libs.ktorfit)
                implementation(libs.webrtc)
                implementation(libs.kermit)
                implementation(libs.jwt)

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.assertk)
                implementation(libs.turbine)
                implementation(libs.ktor.client.mock)
            }
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }

        val nonAndroidTest by creating {
            dependsOn(commonTest.get())
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        iosMain {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        wasmJsMain {
            dependsOn(nonAndroidMain)
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.kotlinx.browser)
            }
        }

        // Configure iosTest/wasmJsTest to depend on nonAndroidTest
        iosTest.get().dependsOn(nonAndroidTest)
        wasmJsTest.get().dependsOn(nonAndroidTest)
    }

    androidLibrary {
        namespace = "ai.anam.lab"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}

buildConfig {
    packageName("ai.anam.lab")
    buildConfigField("String", "VERSION", "\"${providers.gradleProperty("VERSION_NAME").get()}\"")
}

tasks.named("sourcesJar") {
    dependsOn("kspCommonMainKotlinMetadata")
}
