// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.configure

fun Project.configureAndroid() {
    application {
        compileSdk { version = release(Versions.COMPILE_SDK) }

        defaultConfig {
            minSdk = Versions.MIN_SDK
            targetSdk = Versions.TARGET_SDK

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
}

fun Project.configureCompose() {
    if (pluginManager.hasPlugin("com.android.application")) {
        application { buildFeatures.compose = true }
    }
}

private fun Project.application(action: ApplicationExtension.() -> Unit) =
    extensions.configure<ApplicationExtension>(action)

/**
 * Extension function to determine if one of the many Android plugin's has been applied to the Project.
 */
internal fun PluginManager.hasAndroidPlugin(): Boolean = listOf(
    "com.android.application",
    "com.android.kotlin.multiplatform.library",
).any { hasPlugin(it) }

/**
 * Extension function to determine if KMP's androidTarget is required.
 */
internal fun PluginManager.isAndroidTargetRequired(): Boolean = hasPlugin("com.android.application")
