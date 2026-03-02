import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kermit)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.kermit.crashlytics)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.kermit.crashlytics)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.assertk)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.logging"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
