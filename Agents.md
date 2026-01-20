# Agents Context

This document provides context for AI agents (and human developers) working on the Anam codebase. It outlines the project structure, technology stack, development patterns, and build processes.

## Project Overview

Anam is a **Kotlin Multiplatform (KMP)** project targeting **Android** and **iOS**. It uses **Compose Multiplatform** for the UI, allowing for a high degree of code sharing between platforms.

## Tech Stack

-   **Language**: Kotlin 2.x (targeting JDK 21 for build environment)
-   **UI Framework**: Compose Multiplatform
-   **Build System**: Gradle (Kotlin DSL)
-   **Dependency Injection**: [Metro](https://github.com/zacsweers/metro) (Kotlin Multiplatform dependency injection)
-   **HTTP Client**: Ktor
-   **Linting**: Spotless with Ktlint (Kotlin), SwiftLint (Swift), [Compose Rules](https://github.com/mrmans0n/compose-rules)

## Project Structure

The repository follows a monorepo-like structure:

-   **`apps/`**: Platform-specific entry points.
    -   `android`: Android Application module.
    -   `ios`: iOS Application (SwiftUI) + integration layer.
-   **`packages/`**: Shared multiplatform modules.
    -   `app`: The shared Compose UI entry point.
    -   `core`: Infrastructure and utilities.
    -   `domain`: Business logic and data repositories.
    -   `feature`: Feature-specific modules containing UI and presentation logic.
    -   `sdk`: The core SDK module exposing functionality to the apps.
-   **`gradle/`**: Build logic and convention plugins.

    -   `build-logic`: Custom Gradle plugins (Convention Plugins) to standardize build configuration.

## Code Style & Conventions

### Formatting
-   The project uses **Spotless** with **Ktlint** for formatting.
-   **Compose Rules**: The project enforces strict Compose best practices using [Compose Rules](https://github.com/mrmans0n/compose-rules).
-   **Indentation**: 4 spaces.
-   **Trailing Commas**: Encouraged/Enforced, especially in Compose code.

### Architecture
-   **MVVM**: The project appears to follow a Model-View-ViewModel pattern.
-   **State Management**: UI state is typically exposed via `StateFlow` from ViewModels and consumed using `collectAsState()` in Composables.
-   **Features**: Features are modularized in `packages/feature/`.
-   **Dependency Injection**: Uses `dev.zacsweers.metro` (Metro), which provides a Dagger-like experience for Kotlin Multiplatform.

### Naming Conventions
-   **Packages**: `ai.anam.client.<layer>.<module>` (e.g., `ai.anam.client.feature.voices`).

### Resources
-   **Strings**: All string resources are located in `packages/core/ui/resources/src/commonMain/composeResources/values/strings.xml`.
    -   Please keep string resources grouped by screen or feature (e.g., `<!-- Home -->`, `<!-- Settings -->`) to maintain organization.
-   **Images**: Image resources are located in `packages/core/ui/resources/src/commonMain/composeResources/drawable/`.

### Gradle
-   **Plugin Application**: Prefer the `id("plugin-id")` syntax over `alias(libs.plugins.plugin)` in `build.gradle.kts` files for consistency with convention plugins.
    -   Ensure the plugin is applied in the root `build.gradle.kts` with `alias(libs.plugins.plugin) apply false`.
    -   In module `build.gradle.kts`, use `id("plugin-id")` (without version).

### Git Commit Messages
-   **Title**: Start with a capital letter and use the imperative mood (e.g., "Build: Add and configure...").
-   **Format**: `Type: Description` (e.g., `Build:`, `Fix:`, `Feat:`).

## Build & Test

### Prerequisites
-   **JDK**: 21
-   **Xcode**: Required for iOS builds.
-   **CocoaPods**: Required for iOS dependencies (specifically WebRTC SDK).

### Building
-   **Android**:
    ```bash
    ./gradlew :apps:android:assembleDebug
    ```
-   **iOS**:
    -   Requires `pod install` in `apps/ios` (or `./gradlew :packages:sdk:podInstall`).
    -   Open `apps/ios/App.xcworkspace` in Xcode.

### Testing
-   Tests are located in `commonTest` (for multiplatform logic) or platform-specific test source sets.
-   Run all tests:
    ```bash
    ./gradlew check
    ```
    *(Note: Verify specific test tasks if `check` is too broad)*

### Linting
-   **Kotlin**:
    -   Run Spotless check:
        ```bash
        ./gradlew spotlessCheck
        ```
    -   Apply Spotless formatting:
        ```bash
        ./gradlew spotlessApply
        ```
-   **Swift**:
    -   Run SwiftLint:
        ```bash
        swiftlint
        ```

## Convention Plugins
The project uses custom convention plugins defined in `gradle/build-logic`. These centralize configuration for:
-   `ai.anam.client.root`: Root project configuration.
-   `ai.anam.client.multiplatform`: KMP module configuration.
-   `ai.anam.client.android`: Android library/app configuration.
-   `ai.anam.client.compose`: Compose Multiplatform configuration.
-   `ai.anam.client.di`: Dependency injection setup (Metro).

