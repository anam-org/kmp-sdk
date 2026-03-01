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
                api(project(":packages:core:coroutines"))
                api(project(":packages:core:api"))
                api(project(":packages:core:common"))
                api(project(":packages:core:http"))
                api(project(":packages:core:logging"))
                implementation(libs.ktor.client.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                implementation(project(":packages:core:test-fixtures"))
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.core.data"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
