plugins {
    id("ai.anam.lab.client.android.application")
    id("ai.anam.lab.client.compose")
    id("ai.anam.lab.client.di")
    id("ai.anam.lab.client.licensee")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    implementation(projects.packages.app)
    implementation(projects.packages.feature.settings)

    testImplementation(libs.kotlin.test)

    debugImplementation(libs.compose.ui.tooling)
}

android {
    namespace = "ai.anam.lab.client"

    defaultConfig {
        applicationId = "ai.anam.lab.client"
        versionCode = 1
        versionName = "0.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

