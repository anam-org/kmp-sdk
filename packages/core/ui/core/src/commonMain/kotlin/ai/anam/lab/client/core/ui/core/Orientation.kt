package ai.anam.lab.client.core.ui.core

import androidx.compose.runtime.Composable

/**
 * Returns `true` when the device is in landscape orientation.
 *
 * Used by the home screen to switch between a portrait Scaffold layout and a fullscreen
 * landscape video layout. Each platform implements detection differently:
 * - **Android**: reads `LocalConfiguration.current.orientation`.
 * - **iOS**: observes `UIDeviceOrientationDidChangeNotification` for physical device orientation.
 * - **wasmJs**: always returns `false` (landscape fullscreen is not supported on web).
 */
@Composable
expect fun isLandscape(): Boolean
