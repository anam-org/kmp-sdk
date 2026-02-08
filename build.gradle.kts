import de.jensklingenberg.ktorfit.gradle.KtorfitPluginExtension

plugins {
    id("ai.anam.lab.client.root")

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.cacheFixPlugin) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktorfit) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.licensee) apply false
    alias(libs.plugins.buildConfig) apply false
    alias(libs.plugins.maven.publish) apply false
}

tasks.register("printVersion") {
    val versionName = project.property("VERSION_NAME")
    doLast {
        println(versionName)
    }
}

subprojects {
    pluginManager.withPlugin("de.jensklingenberg.ktorfit") {
        extensions.configure<KtorfitPluginExtension> {
            compilerPluginVersion.set(libs.versions.ktorfit.compiler)
        }
    }
}
