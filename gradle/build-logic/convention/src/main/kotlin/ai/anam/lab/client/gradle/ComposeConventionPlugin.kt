// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import kotlin.jvm.optionals.getOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        // Check to see if our target is KMP. If so, include the CMP plugin.
        if (pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            pluginManager.apply("org.jetbrains.compose")
        }

        configureCompose()

        // Attempt to use the BOM. This allows us to better manage versions by simply control this one dependency.
        val bom = libs.findLibrary("androidx-compose-bom").getOrNull()
        if (bom != null) {
            val platformBom = dependencies.platform(bom)
            when {
                pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform") ->
                    extensions.configure<KotlinMultiplatformExtension> {
                        sourceSets.getByName("commonMain").dependencies {
                            implementation(platformBom)
                        }
                    }
                else ->
                    dependencies {
                        add("implementation", platformBom)
                    }
            }
        }
    }
}
