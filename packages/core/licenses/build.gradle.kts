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
                api(project(":packages:core:logging"))
                implementation(project(":packages:core:ui:resources"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.licenses"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
