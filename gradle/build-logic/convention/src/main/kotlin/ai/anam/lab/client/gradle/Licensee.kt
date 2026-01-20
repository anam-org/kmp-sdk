// Copyright 2024, Anam.ai
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import app.cash.licensee.LicenseeExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun Project.configureLicensee() {
    with(pluginManager) {
        apply("app.cash.licensee")
    }

    extensions.configure<LicenseeExtension> {
        allow("Apache-2.0")
        allow("MIT")
        allow("BSD-3-Clause")
        allowUrl("https://github.com/icerockdev/moko-permissions/blob/master/LICENSE.md") {
            because("Apache-2.0")
        }
        allowUrl("https://opensource.org/license/bsd-3-clause/") {
            because("BSD-3-Clause")
        }
        allowUrl("https://opensource.org/license/mit") {
            because("MIT")
        }
    }

    // Check if this project should export license reports
    // Only the 'shared' package (packages/app) needs to export reports to resources
    if (path == ":packages:app") {
        configureLicenseeReportExport()
    }
}

private fun Project.configureLicenseeReportExport() {
    // Android Variants
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryAndroidComponentsExtension> {
            onVariants { variant ->
                val variantName = variant.name
                val capitalizedVariantName = variantName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val licenseeTaskName = "licenseeAndroid$capitalizedVariantName"
                val copyTaskName = "copyLicenseeReportAndroid$capitalizedVariantName"

                registerCopyLicenseeReportTask(
                    taskName = copyTaskName,
                    licenseeTaskName = licenseeTaskName,
                    reportPath = "reports/licensee/android$capitalizedVariantName/artifacts.json",
                    sourceName = "android$capitalizedVariantName",
                )

                // Use configure() or wrap in logic to wait for task creation if needed,
                // but onVariants runs early. "assembleDebug" should be created by AGP.
                // However, sometimes finding it by name fails if it's not yet registered.
                // We can try using the task provider if we can access it, or use afterEvaluate (discouraged but works),
                // or better: configure the task graph.
                //
                // AGP 7/8+ separates variant API from task creation. Tasks might be created later.
                // We can try to use `tasks.configureEach` or similar lazy lookup.
                tasks.configureEach {
                    if (name == "assemble$capitalizedVariantName") {
                        finalizedBy(copyTaskName)
                    }
                }
            }
        }
    }

    // iOS Targets
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            targets.withType<KotlinNativeTarget> {
                if (konanTarget.family.isAppleFamily) {
                    val targetName = this.name
                    val capitalizedTargetName = targetName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    val licenseeTaskName = "licensee$capitalizedTargetName"
                    val copyTaskName = "copyLicenseeReport$capitalizedTargetName"

                    registerCopyLicenseeReportTask(
                        taskName = copyTaskName,
                        licenseeTaskName = licenseeTaskName,
                        reportPath = "reports/licensee/$targetName/artifacts.json",
                        sourceName = targetName,
                    )

                    compilations.getByName("main").compileTaskProvider.configure {
                        finalizedBy(copyTaskName)
                    }
                }
            }
        }
    }
}

private fun Project.registerCopyLicenseeReportTask(
    taskName: String,
    licenseeTaskName: String,
    reportPath: String,
    sourceName: String,
) {
    tasks.register(taskName) {
        dependsOn(licenseeTaskName)

        val outputDir = project(":packages:core:ui:resources").file("src/commonMain/composeResources/files")
        val inputFile = layout.buildDirectory.file(reportPath)

        doLast {
            if (inputFile.get().asFile.exists()) {
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                inputFile.get().asFile.copyTo(outputDir.resolve("licenses.json"), overwrite = true)
                println("Copied licenses.json from $sourceName to ${outputDir.absolutePath}")
            } else {
                println("Licensee report not found at ${inputFile.get().asFile.absolutePath}")
            }
        }
    }
}
