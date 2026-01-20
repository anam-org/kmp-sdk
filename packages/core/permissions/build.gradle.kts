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
                api(libs.compose.runtime)
                implementation(libs.moko.permissions.core)
                implementation(libs.moko.permissions.microphone)
            }
        }

        commonTest {
            dependencies {
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }
    }
}

android {
    namespace = "ai.anam.lab.client.core.permissions"
}
