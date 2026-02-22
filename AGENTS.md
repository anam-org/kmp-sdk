# Agents Context

This document provides context for AI agents (and human developers) working on the Anam codebase. It outlines the project structure, technology stack, development patterns, and build processes.

## Project Overview

Anam is a **Kotlin Multiplatform (KMP)** project targeting **Android**, **iOS**, and **Web (wasmJs/browser)**. It uses **Compose Multiplatform** for the UI, allowing for a high degree of code sharing between platforms.

## Tech Stack

-   **Language**: Kotlin 2.x (targeting JDK 21 for build environment)
-   **UI Framework**: Compose Multiplatform
-   **Build System**: Gradle (Kotlin DSL)
-   **Dependency Injection**: [Metro](https://github.com/zacsweers/metro) (Kotlin Multiplatform dependency injection)
-   **HTTP Client**: [Ktorfit](https://github.com/Foso/Ktorfit) 2.7+ (Retrofit-like KSP code-gen wrapper over Ktor)
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
-   **Required after every edit**: Always run `./gradlew spotlessApply` after modifying any Kotlin files to ensure formatting is correct before committing. Do not skip this step.

### Architecture
-   **MVVM**: The project follows a Model-View-ViewModel pattern.
-   **State Management**: ViewModels extend `BaseViewModel<S : ViewState>`. State is updated via `setState { copy(...) }` and collected in Composables via `state.collectAsState()`. Retrieve ViewModels in Composables using the `metroViewModel()` helper.
-   **Error Handling**: Uses `Either<L, R>` (Left = error, Right = success) and an `ApiResult` sealed class.
-   **Repository Pattern**: Repositories call `cancellableRunCatching { apiCall { ... } }.fold(...)` on the IO dispatcher.
-   **Domain Interactors**: Defined as `fun interface` types and wired in `DomainDataSubgraph`.
-   **Features**: Features are modularized in `packages/feature/`. Each feature registers via two DI subgraphs:
    -   `FeatureSubgraph` (`@ContributesTo(AppScope::class)`) — registers `FeatureContent` via `@FeatureRouteKey`.
    -   `FeatureViewSubgraph` (`@ContributesTo(ViewModelScope::class)`) — registers the ViewModel via `@ViewModelKey`.
-   **DI Scopes**: `AppScope` for app-level singletons; `ViewModelScope` for ViewModel-scoped bindings.
-   **Dependency Injection**: Uses `dev.zacsweers.metro` (Metro), which provides a Dagger-like experience for Kotlin Multiplatform.

### Testing
-   **Test class structure**: In unit test classes, place `@Test` methods first, then private helper functions (e.g., factory methods like `createViewModel()`), then fake/test-double classes at the end.
-   **Test Fixtures**: Fake implementations of core interfaces (e.g., `FakeLogger`, `FakeNavigator`, `FakeAnamPreferences`) live in the `packages/core/test-fixtures` module under the `ai.anam.lab.client.core.test` package. Consumer modules depend on this via `commonTest` dependencies. Do not duplicate fakes in individual module test source sets.

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
-   **Co-authors**: Do not add AI assistants (e.g., Claude) as co-authors in commit messages.

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

## Kotlin/Wasm JS Interop

The `wasmJs` target has stricter interop rules than Kotlin/JS:

-   **`js()` calls**: Must be package-level function bodies containing a string literal only — no inline expressions.
    ```kotlin
    fun myJsHelper(cb: (JsAny) -> Unit) = js("someJsApi(cb)")
    ```
-   **Parameters**: `js()` can capture Kotlin function parameters (including lambdas typed as `(JsAny) -> Unit`).
-   **No `asDynamic()`**: This is Kotlin/JS only and does not exist in Kotlin/Wasm.
-   **ArrayBuffer → ByteArray**: Use `org.khronos.webgl.Int8Array(buffer).toByteArray()`.

## Convention Plugins
The project uses custom convention plugins defined in `gradle/build-logic`. These centralize configuration for:
-   `ai.anam.client.root`: Root project configuration.
-   `ai.anam.client.multiplatform`: KMP module configuration.
-   `ai.anam.client.android`: Android library/app configuration.
-   `ai.anam.client.compose`: Compose Multiplatform configuration.
-   `ai.anam.client.di`: Dependency injection setup (Metro).

