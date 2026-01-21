plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
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
    namespace = "ai.anam.lab.client.core.notifications"
}
