plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":packages:core:logging"))
                implementation(project(":packages:core:ui:resources"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

android {
    namespace = "ai.anam.lab.client.core.licenses"
}
