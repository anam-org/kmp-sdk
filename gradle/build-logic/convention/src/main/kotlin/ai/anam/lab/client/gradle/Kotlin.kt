package ai.anam.lab.client.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Configures experimental Kotlin compiler flags that must be applied consistently across all modules.
 */
fun Project.configureKotlin() {
    // https://kotlinlang.org/docs/whatsnew23.html#explicit-backing-fields (experimental)
    tasks.withType(KotlinCompilationTask::class.java).configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xexplicit-backing-fields")
        }
    }
}
