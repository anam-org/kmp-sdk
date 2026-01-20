plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.multiplatform.settings.core)
                api(libs.multiplatform.settings.coroutines)
                api(project(":packages:core:coroutines"))

                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
            }
        }
    }
}

android {
    namespace = "ai.anam.lab.client.core.settings"
}
