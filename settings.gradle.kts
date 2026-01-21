rootProject.name = "Anam"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":apps:android")

include(":packages:app")

include(":packages:core:api")
include(":packages:core:auth")
include(":packages:core:client")
include(":packages:core:common")
include(":packages:core:datetime")
include(":packages:core:coroutines")
include(":packages:core:data")
include(":packages:core:di")
include(":packages:core:http")
include(":packages:core:licenses")
include(":packages:core:logging")
include(":packages:core:navigation")
include(":packages:core:notifications")
include(":packages:core:permissions")
include(":packages:core:settings")
include(":packages:core:viewmodel")
include(":packages:core:ui:core")
include(":packages:core:ui:components")
include(":packages:core:ui:imageloading")
include(":packages:core:ui:resources")
include(":packages:core:ui:theme")
include(":packages:core:ui:video")

include(":packages:domain:data")
include(":packages:domain:notifications")
include(":packages:domain:permissions")
include(":packages:domain:sessions")

include(":packages:feature:home")
include(":packages:feature:avatars")
include(":packages:feature:voices")
include(":packages:feature:llms")
include(":packages:feature:notifications")
include(":packages:feature:session")
include(":packages:feature:messages")
include(":packages:feature:settings")
include(":packages:feature:licenses")

include(":packages:sdk")
