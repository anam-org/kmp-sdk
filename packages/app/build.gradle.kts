import ai.anam.lab.client.gradle.Versions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
    id("ai.anam.lab.client.licensee")
}

kotlin {
    configure(targets) {
        if (this is KotlinNativeTarget) {
            binaries.framework {
                baseName = "Shared"
                isStatic = true

                export(project(":packages:core:api"))
                export(project(":packages:core:client"))
                export(project(":packages:core:data"))
                export(project(":packages:core:coroutines"))
                export(project(":packages:core:di"))
                export(project(":packages:core:http"))
                export(project(":packages:core:licenses"))
                export(project(":packages:core:logging"))
                export(project(":packages:core:navigation"))
                export(project(":packages:core:permissions"))
                export(project(":packages:core:settings"))
                export(project(":packages:core:viewmodel"))
                export(project(":packages:core:notifications"))
                export(project(":packages:core:ui:core"))
                export(project(":packages:core:ui:imageloading"))
                export(project(":packages:core:ui:theme"))

                export(project(":packages:domain:data"))
                export(project(":packages:domain:notifications"))
                export(project(":packages:domain:permissions"))
                export(project(":packages:domain:sessions"))

                export(project(":packages:feature:home"))
                export(project(":packages:feature:avatars"))
                export(project(":packages:feature:session"))
                export(project(":packages:feature:messages"))
                export(project(":packages:feature:voices"))
                export(project(":packages:feature:llms"))
                export(project(":packages:feature:notifications"))
                export(project(":packages:feature:settings"))
                export(project(":packages:feature:licenses"))
                export(project(":packages:feature:create"))
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.compose.foundation)
                api(libs.compose.ui)

                api(libs.androidx.navigation.compose)

                api(libs.coil.compose)

                api(project(":packages:core:api"))
                api(project(":packages:core:client"))
                api(project(":packages:core:coroutines"))
                api(project(":packages:core:data"))
                api(project(":packages:core:di"))
                api(project(":packages:core:http"))
                api(project(":packages:core:licenses"))
                api(project(":packages:core:logging"))
                api(project(":packages:core:navigation"))
                api(project(":packages:core:permissions"))
                api(project(":packages:core:settings"))
                api(project(":packages:core:viewmodel"))
                api(project(":packages:core:notifications"))
                api(project(":packages:core:ui:core"))
                api(project(":packages:core:ui:imageloading"))
                api(project(":packages:core:ui:theme"))

                api(project(":packages:domain:data"))
                api(project(":packages:domain:notifications"))
                api(project(":packages:domain:permissions"))
                api(project(":packages:domain:sessions"))

                api(project(":packages:feature:home"))
                api(project(":packages:feature:avatars"))
                api(project(":packages:feature:session"))
                api(project(":packages:feature:messages"))
                api(project(":packages:feature:voices"))
                api(project(":packages:feature:llms"))
                api(project(":packages:feature:notifications"))
                api(project(":packages:feature:settings"))
                api(project(":packages:feature:licenses"))
                api(project(":packages:feature:create"))
            }
        }
    }

    androidLibrary {
        namespace = "ai.anam.lab.client.shared"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}
