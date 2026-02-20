import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
    id("ai.anam.lab.client.compose")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.compose.runtime)
            }
        }

        commonTest {
            dependencies {
            }
        }

        androidMain {
            dependencies {
                implementation(libs.moko.permissions.core)
                implementation(libs.moko.permissions.microphone)
                implementation(libs.moko.permissions.camera)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.moko.permissions.core)
                implementation(libs.moko.permissions.microphone)
                implementation(libs.moko.permissions.camera)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.permissions"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
