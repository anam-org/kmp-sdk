// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint()
    }

    kotlinGradle {
        target("*.kts")
        ktlint()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.licensee.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("root") {
            id = "ai.anam.lab.client.root"
            implementationClass = "ai.anam.lab.client.gradle.RootConventionPlugin"
        }

        register("kotlinMultiplatform") {
            id = "ai.anam.lab.client.multiplatform"
            implementationClass = "ai.anam.lab.client.gradle.KotlinMultiplatformConventionPlugin"
        }

        register("kotlinAndroid") {
            id = "ai.anam.lab.client.android"
            implementationClass = "ai.anam.lab.client.gradle.KotlinAndroidConventionPlugin"
        }

        register("androidApplication") {
            id = "ai.anam.lab.client.android.application"
            implementationClass = "ai.anam.lab.client.gradle.AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = "ai.anam.lab.client.android.library"
            implementationClass = "ai.anam.lab.client.gradle.AndroidLibraryConventionPlugin"
        }

        register("compose") {
            id = "ai.anam.lab.client.compose"
            implementationClass = "ai.anam.lab.client.gradle.ComposeConventionPlugin"
        }

        register("di") {
            id = "ai.anam.lab.client.di"
            implementationClass = "ai.anam.lab.client.gradle.MetroConventionPlugin"
        }

        register("licensee") {
            id = "ai.anam.lab.client.licensee"
            implementationClass = "ai.anam.lab.client.gradle.LicenseeConventionPlugin"
        }
    }
}
