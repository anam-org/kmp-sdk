// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()

            if (pluginManager.hasAndroidPlugin()) {
                androidTarget()
            } else {
                jvm()
            }

            iosArm64()
            iosSimulatorArm64()

            compilerOptions {
                // https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-expect-actual.html#expected-and-actual-classes
                freeCompilerArgs.add("-Xexpect-actual-classes")
                freeCompilerArgs.add("-Xannotation-default-target=param-property")

                freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
            }

            metadata {
                compilations.configureEach {
                    if (name == KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
                        compileTaskProvider.configure {
                            // We replace the default library names with something more unique (the project path).
                            // This allows us to avoid the annoying issue of `duplicate library name: foo_commonMain`
                            // https://youtrack.jetbrains.com/issue/KT-57914
                            val projectPath = this@with.path.substring(1).replace(":", "_")
                            this as KotlinCompileCommon
                            moduleName.set("${projectPath}_commonMain")
                        }
                    }
                }
            }

            configureSpotless()
            configureKotlin()

            // Note: Disable Skie, at least for now. We can decide later whether to use Skie to build our Swift
            // interface or just do this manually.
            // if (path.endsWith(":packages:app")) {
            //    pluginManager.apply("co.touchlab.skie")
            //    configureSkie()
            //}
        }
    }
}
