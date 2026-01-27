// Copyright 2024, Anam.ai
// SPDX-License-Identifier: Apache-2.0

package ai.anam.lab.client.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class LicenseeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureLicensee()
    }
}
