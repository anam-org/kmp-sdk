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
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.lazy.paginated.compose)
                implementation(libs.coil.compose)

                api(project(":packages:core:logging"))
                implementation(project(":packages:core:common"))
                implementation(project(":packages:core:datetime"))
                implementation(project(":packages:core:di"))
                implementation(project(":packages:core:navigation"))
                implementation(project(":packages:core:viewmodel"))
                implementation(project(":packages:core:ui:components"))
                implementation(project(":packages:core:ui:resources"))

                implementation(project(":packages:domain:data"))
            }
        }

        commonTest {
            dependencies {
            }
        }
    }
}

android {
    namespace = "ai.anam.lab.client.feature.avatars"
}
