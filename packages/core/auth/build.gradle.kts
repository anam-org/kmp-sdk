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
