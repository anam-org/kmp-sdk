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
                api(libs.androidx.lifecycle.viewmodelCompose)
            }
        }

        commonTest {
            dependencies {
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.di"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
