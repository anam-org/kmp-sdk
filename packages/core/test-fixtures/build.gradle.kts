import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":packages:core:logging"))
                implementation(project(":packages:core:navigation"))
                implementation(project(":packages:core:settings"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.test"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
