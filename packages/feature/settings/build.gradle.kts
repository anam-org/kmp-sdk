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
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.lifecycle.viewmodelCompose)

                api(project(":packages:core:logging"))
                implementation(project(":packages:core:common"))
                implementation(project(":packages:core:di"))
                implementation(project(":packages:core:navigation"))
                implementation(project(":packages:core:viewmodel"))
                implementation(project(":packages:core:settings"))
                implementation(project(":packages:core:ui:components"))
                implementation(project(":packages:core:ui:resources"))

                implementation(project(":packages:domain:data"))
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(project(":packages:core:test-fixtures"))
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.feature.settings"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
