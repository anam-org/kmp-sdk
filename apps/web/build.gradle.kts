@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
    id("ai.anam.lab.client.licensee")
}

kotlin {
    wasmJs {
        outputModuleName = "web"
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(project(":packages:app"))
            }
        }
    }
}
