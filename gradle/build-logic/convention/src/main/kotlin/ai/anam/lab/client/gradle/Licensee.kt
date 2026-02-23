// Copyright 2024, Anam.ai
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import app.cash.licensee.LicenseeExtension
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

    // Only the shared package (packages/app) needs to export license reports to resources
    if (path == ":packages:app") {
        configureLicenseeReportExport()
    }
}

private fun Project.configureLicenseeReportExport() {
    val appProjectPath = this.path
    val resourceProject = project(":packages:core:ui:resources")

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            // iOS targets — each uses its own licensee report
            targets.withType<KotlinNativeTarget> {
                if (konanTarget.family.isAppleFamily) {
                    val targetName = this.name
                    val capitalizedTargetName = targetName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    registerCopyLicenseeReportTask(
                        taskName = "copyLicenseeReport$capitalizedTargetName",
                        licenseeTaskName = "licensee$capitalizedTargetName",
                        reportPath = "reports/licensee/$targetName/artifacts.json",
                        sourceName = targetName,
                    )

                    wireComposeResourceDependency(
                        resourceProject = resourceProject,
                        appProjectPath = appProjectPath,
                        copyTaskName = "copyLicenseeReport$capitalizedTargetName",
                        platformResourceTaskSuffix = "${capitalizedTargetName}Main",
                    )
                }
            }
        }

        // WasmJS — use its own licensee report
        registerCopyLicenseeReportTask(
            taskName = "copyLicenseeReportWasmJs",
            licenseeTaskName = "licenseeWasmJs",
            reportPath = "reports/licensee/wasmJs/artifacts.json",
            sourceName = "wasmJs",
        )

        wireComposeResourceDependency(
            resourceProject = resourceProject,
            appProjectPath = appProjectPath,
            copyTaskName = "copyLicenseeReportWasmJs",
            platformResourceTaskSuffix = "WasmJsMain",
        )

        // Android — the KMP Android plugin (com.android.kotlin.multiplatform.library) in
        // :packages:app does not produce a licensee report, but :apps:android applies
        // com.android.application which does. Use that project's release report since
        // the release variant is what gets distributed to users.
        val androidAppProject = project(":apps:android")
        registerCopyLicenseeReportTask(
            taskName = "copyLicenseeReportAndroid",
            licenseeTaskName = "${androidAppProject.path}:licenseeAndroidRelease",
            reportPath = "reports/licensee/androidRelease/artifacts.json",
            sourceName = "androidRelease",
            sourceProject = androidAppProject,
        )

        wireComposeResourceDependency(
            resourceProject = resourceProject,
            appProjectPath = appProjectPath,
            copyTaskName = "copyLicenseeReportAndroid",
            platformResourceTaskSuffix = "AndroidMain",
        )
    }
}

/**
 * Wires the license copy task into the compose resource processing pipeline for a given platform.
 *
 * The platform-specific prepare task (e.g. `prepareComposeResourcesTaskForAndroidMain`) gains a
 * [dependsOn] on the copy task, pulling it into the task graph only when that platform is built.
 * The common resource tasks use [mustRunAfter] so they wait for the copy when it is in the graph,
 * without forcing other platforms' licensee tasks to run.
 */
private fun wireComposeResourceDependency(
    resourceProject: Project,
    appProjectPath: String,
    copyTaskName: String,
    platformResourceTaskSuffix: String,
) {
    val copyTaskPath = "$appProjectPath:$copyTaskName"
    resourceProject.tasks.configureEach {
        when {
            name == "prepareComposeResourcesTaskFor$platformResourceTaskSuffix" -> {
                dependsOn(copyTaskPath)
            }
            name == "copyNonXmlValueResourcesForCommonMain" ||
                name == "prepareComposeResourcesTaskForCommonMain" -> {
                mustRunAfter(copyTaskPath)
            }
        }
    }
}

private fun Project.registerCopyLicenseeReportTask(
    taskName: String,
    licenseeTaskName: String,
    reportPath: String,
    sourceName: String,
    sourceProject: Project = this,
) {
    tasks.register(taskName) {
        dependsOn(licenseeTaskName)

        val outputDir = project(":packages:core:ui:resources").file("src/commonMain/composeResources/files")
        val inputFile = sourceProject.layout.buildDirectory.file(reportPath)

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
