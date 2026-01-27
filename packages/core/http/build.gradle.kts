import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)
                api(libs.ktorfit)
                api(project(":packages:core:auth"))
                api(project(":packages:core:logging"))

                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
                implementation(libs.ktor.client.auth)
            }
        }

        commonTest {
            dependencies {
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.http"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
