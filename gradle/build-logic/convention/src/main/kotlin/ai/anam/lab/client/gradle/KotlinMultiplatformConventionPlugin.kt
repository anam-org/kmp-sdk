// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.CompileUsingKotlinDaemon
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()

            when {
                pluginManager.hasAndroidPlugin() -> {
                    // The new Android Library plugin automatically handles the androidLibrary target.
                    if (pluginManager.isAndroidTargetRequired()) {
                        androidTarget()
                    }
                }

                else -> {
                    jvm()
                }
            }

            iosArm64()
            iosSimulatorArm64()

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
            }

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
        }

        // Workaround for KT-82395: "No file found for source null" when compiler plugins
        // (Metro, Compose, etc.) generate top-level declarations with incremental compilation.
        // Use OUT_OF_PROCESS strategy which disables incremental for wasmJs only.
        tasks.configureEach {
            if (this is CompileUsingKotlinDaemon && name.contains("WasmJs")) {
                compilerExecutionStrategy.set(KotlinCompilerExecutionStrategy.OUT_OF_PROCESS)
            }
        }
    }
}
