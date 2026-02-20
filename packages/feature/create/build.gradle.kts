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
                implementation(libs.coil.compose)

                api(project(":packages:core:logging"))
                implementation(project(":packages:core:common"))
                implementation(project(":packages:core:di"))
                implementation(project(":packages:core:navigation"))
                implementation(project(":packages:core:compression"))
                implementation(project(":packages:core:viewmodel"))
                implementation(project(":packages:core:ui:core"))
                implementation(project(":packages:core:ui:resources"))

                implementation(project(":packages:core:permissions"))

                implementation(project(":packages:domain:data"))
                implementation(project(":packages:domain:permissions"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.peekaboo.ui)
                implementation(libs.peekaboo.image.picker)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.peekaboo.ui)
                implementation(libs.peekaboo.image.picker)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.feature.create"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
