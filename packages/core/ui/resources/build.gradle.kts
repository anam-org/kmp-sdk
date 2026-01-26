import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ai.anam.lab.client.core.ui.resources.generated.resources"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
            }
        }

        commonTest {
            dependencies {
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.ui.resources"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK

        androidResources {
            enable = true
        }
    }
}
