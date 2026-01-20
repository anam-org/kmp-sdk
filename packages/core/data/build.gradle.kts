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
            }
        }

        commonTest {
            dependencies {
            }
        }
    }
}

android {
    namespace = "ai.anam.lab.client.core.data"
}
