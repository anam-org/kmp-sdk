plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":packages:sdk"))
                api(libs.kotlinx.coroutines.core)
                api(project(":packages:core:auth"))
                api(project(":packages:core:data"))
                implementation(project(":packages:core:logging"))
            }
        }

        commonTest {
            dependencies {
            }
        }
    }
}

android {
    namespace = "ai.anam.lab.client.core.client"
}
