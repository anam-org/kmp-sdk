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
                implementation(project(":packages:core:ui:video"))
                implementation(project(":packages:core:ui:resources"))

                implementation(project(":packages:domain:data"))
                implementation(project(":packages:domain:notifications"))
                implementation(project(":packages:domain:permissions"))
                implementation(project(":packages:domain:sessions"))

                implementation(project(":packages:feature:session"))
                implementation(project(":packages:feature:avatars"))
                implementation(project(":packages:feature:messages"))
                implementation(project(":packages:feature:voices"))
                implementation(project(":packages:feature:llms"))

                implementation(project(":packages:sdk"))
            }
        }

        commonTest {
            dependencies {
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.feature.home"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
