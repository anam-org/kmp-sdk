package ai.anam.lab.client.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureSpotless() {
    with(pluginManager) {
        apply("com.diffplug.spotless")
    }

    spotless {
        // Extract the version of ktlint being used via the version catalog.
        val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion

        val composeRulesVersion = libs.findVersion("compose-rules").get().requiredVersion

        kotlin {
            target("src/**/*.kt")
            ktlint(ktlintVersion)
                .editorConfigOverride(
                    mapOf(
                        "compose_allowed_composition_locals" to "LocalPreferences,LocalViewModelGraphProvider",
                        "compose_allowed_lambda_parameter_names" to "onFirstFrameRendered",
                    ),
                )
                .customRuleSets(
                    listOf(
                        "io.nlopez.compose.rules:ktlint:$composeRulesVersion",
                    ),
                )
        }

        kotlinGradle {
            target("*.kts")
            ktlint(ktlintVersion)
        }
    }
}

private fun Project.spotless(action: SpotlessExtension.() -> Unit) = extensions.configure<SpotlessExtension>(action)
