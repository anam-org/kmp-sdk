plugins {
    id("ai.anam.lab.client.android.application")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
    id("ai.anam.lab.client.licensee")
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)

    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    implementation(projects.packages.app)
    implementation(projects.packages.feature.settings)

    testImplementation(libs.kotlin.test)

    debugImplementation(libs.compose.ui.tooling)
}

android {
    namespace = "ai.anam.lab.client"

    val keystorePath = providers.environmentVariable("KEYSTORE_PATH")

    if (keystorePath.isPresent) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath.get())
                storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("KEY_PASSWORD").get()
            }
        }
    }

    defaultConfig {
        applicationId = "ai.anam.lab.client"
        versionCode = providers.environmentVariable("APP_VERSION_CODE")
            .orElse(providers.gradleProperty("APP_VERSION_CODE"))
            .getOrElse("1")
            .toInt()
        versionName = providers.environmentVariable("APP_VERSION_NAME")
            .orElse(providers.gradleProperty("APP_VERSION_NAME"))
            .getOrElse("0.1")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (keystorePath.isPresent) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

