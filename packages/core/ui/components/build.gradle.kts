import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(project(":packages:core:ui:resources"))
            }
        }

        commonTest {
            dependencies {
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.ui.components"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
