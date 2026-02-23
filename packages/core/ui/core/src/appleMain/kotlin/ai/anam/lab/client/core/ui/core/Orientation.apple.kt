package ai.anam.lab.client.core.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIDeviceOrientationDidChangeNotification

/**
 * Detects landscape orientation using physical device orientation only.
 *
 * ## Why not `LocalWindowInfo.containerSize`?
 *
 * Compose's container size reflects the current interface orientation, not the physical device
 * orientation. When the Compose tree changes drastically (e.g. switching from a Scaffold to a
 * fullscreen video layout), `ComposeUIViewController` calls `requestGeometryUpdate(.portrait)`,
 * which forces the interface back to portrait. This causes `containerSize` to report portrait
 * dimensions even though the device is in landscape, creating a feedback loop:
 *
 * 1. Device rotates to landscape → `containerSize` reports landscape → `isLandscape = true`
 * 2. Compose tree switches to fullscreen → `ComposeUIViewController` forces portrait
 * 3. `containerSize` reports portrait → `isLandscape = false` → tree reverts to Scaffold
 * 4. Repeat from step 1
 *
 * Using [UIDevice.currentDevice] orientation avoids this entirely because it reflects the
 * physical device state, which is unaffected by programmatic interface orientation changes.
 *
 * ## Companion: `AnanAppDelegate.orientationLock`
 *
 * This composable works in tandem with the Swift-side orientation lock in `AnanAppDelegate`.
 * Both observe `UIDeviceOrientationDidChangeNotification`, but the Swift observer fires
 * **synchronously** (selector-based API) while this Kotlin observer fires **asynchronously**
 * (queued on [NSOperationQueue.mainQueue]). This ordering ensures the orientation lock is set
 * before this composable triggers a recomposition, preventing `ComposeUIViewController` from
 * successfully forcing portrait.
 */
@Composable
actual fun isLandscape(): Boolean {
    var isLandscape by remember { mutableStateOf(false) }

    // Note: beginGeneratingDeviceOrientationNotifications() is called once by
    // AnanAppDelegate at launch and remains active for the app's lifetime, so
    // we don't need to call begin/end here.
    DisposableEffect(Unit) {
        // Set initial value from current device orientation
        isLandscape = UIDevice.currentDevice.orientation.isDeviceLandscape()

        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIDeviceOrientationDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ ->
            val orientation = UIDevice.currentDevice.orientation
            when (orientation) {
                UIDeviceOrientation.UIDeviceOrientationLandscapeLeft,
                UIDeviceOrientation.UIDeviceOrientationLandscapeRight,
                -> isLandscape = true
                UIDeviceOrientation.UIDeviceOrientationPortrait,
                UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown,
                -> isLandscape = false
                // Ignore faceUp, faceDown, unknown
                else -> {}
            }
        }

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    return isLandscape
}

private fun UIDeviceOrientation.isDeviceLandscape(): Boolean =
    this == UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ||
        this == UIDeviceOrientation.UIDeviceOrientationLandscapeRight
