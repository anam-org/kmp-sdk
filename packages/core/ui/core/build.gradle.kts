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
                implementation(libs.compose.ui)
                implementation(project(":packages:core:settings"))
            }
        }

        commonTest {
            dependencies {
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.core)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.ui.core"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
