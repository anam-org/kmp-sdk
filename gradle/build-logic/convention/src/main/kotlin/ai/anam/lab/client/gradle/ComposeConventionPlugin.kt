// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import kotlin.jvm.optionals.getOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        // Check to see if our target is KMP. If so, include the CMP plugin.
        if (pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            pluginManager.apply("org.jetbrains.compose")
        }

        // Check to see if we're building for Android, to configure the necessary build feature,
        if (pluginManager.hasAndroidPlugin()) {
            android {
                buildFeatures.compose = true
            }
        }

        // Attempt to use the BOM. This allows us to better manage versions by simply control this one dependency.
        val bom = libs.findLibrary("androidx-compose-bom").getOrNull()
        if (bom != null) {
            dependencies {
                add("implementation", platform(bom))
            }
        }
    }
}
