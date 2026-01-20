// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.configure

fun Project.configureAndroid() {
    android {
        compileSdkVersion(Versions.COMPILE_SDK)

        defaultConfig {
            minSdk = Versions.MIN_SDK
            targetSdk = Versions.TARGET_SDK

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
}

internal fun Project.android(action: BaseExtension.() -> Unit) = extensions.configure<BaseExtension>(action)

/**
 * Extension function to determine if one of the many Android plugin's has been applied to the Project.
 */
internal fun PluginManager.hasAndroidPlugin(): Boolean = listOf(
    "com.android.library",
    "com.android.application",
).any { hasPlugin(it) }
