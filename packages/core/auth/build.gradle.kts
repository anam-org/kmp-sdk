import ai.anam.lab.client.gradle.Versions

plugins {
    id("ai.anam.lab.client.android.library")
    id("ai.anam.lab.client.multiplatform")
    id("ai.anam.lab.client.di")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":packages:core:settings"))
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
        namespace = "ai.anam.lab.client.core.auth"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
    }
}

buildConfig {
    packageName("ai.anam.lab.client.core.auth")

    val localPropertiesFile = rootProject.file("local.properties")
    val apiTokenProvider = providers.gradleProperty("API_TOKEN")
        .orElse(
            providers.provider {
                if (localPropertiesFile.exists()) {
                    val lines = localPropertiesFile.readLines()
                    lines.firstOrNull { it.startsWith("API_TOKEN=") }
                        ?.substringAfter("=", "")
                        ?.trim()
                        ?: ""
                } else {
                    ""
                }
            },
        )

    buildConfigField("String", "API_TOKEN", "\"${apiTokenProvider.get()}\"")
}
