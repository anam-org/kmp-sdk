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
                api(libs.coil.network.ktor3)
            }
        }

        commonTest {
            dependencies {
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.coil.wasm.js)
                implementation(libs.coil.network.ktor3.wasm.js)
                implementation(libs.ktor.client.js)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.ui.imageloading"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
