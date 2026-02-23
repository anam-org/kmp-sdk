import UIKit
import Shared

/// App delegate responsible for setting up Compose Multiplatform and managing orientation.
///
/// ## Orientation Lock
///
/// Compose Multiplatform's `ComposeUIViewController` calls `requestGeometryUpdate(.portrait)`
/// during recomposition when the Compose tree changes drastically (e.g. switching between a
/// portrait Scaffold and a fullscreen landscape video layout). This forces the interface back
/// to portrait even when the device is physically in landscape, regardless of the view
/// controller's `supportedInterfaceOrientations`.
///
/// To prevent this, the app delegate dynamically restricts the allowed orientations via
/// `application(_:supportedInterfaceOrientationsFor:)`. When the device is physically in
/// landscape, only landscape orientations are permitted, causing the system to reject the
/// forced portrait request. When the device returns to portrait, all orientations are allowed
/// again.
///
/// ### Timing
///
/// The Swift-side orientation observer uses the selector-based `NotificationCenter` API, which
/// delivers notifications **synchronously**. The Kotlin-side observer in `Orientation.apple.kt`
/// uses `NSOperationQueue.mainQueue`, which delivers **asynchronously**. This guarantees the
/// orientation lock is set *before* Compose recomposes in response to the same device rotation.
///
/// ### Companion: `Orientation.apple.kt`
///
/// The Kotlin-side `isLandscape()` composable also observes `UIDeviceOrientationDidChangeNotification`
/// to drive the Compose UI state. It intentionally uses device orientation rather than
/// `LocalWindowInfo.containerSize` to avoid a feedback loop — see `Orientation.apple.kt` for details.
class AnanAppDelegate: NSObject, UIApplicationDelegate {
    /// Dynamically restricts allowed interface orientations.
    ///
    /// - `.landscape`: Set when the device is physically in landscape. Prevents
    ///   `ComposeUIViewController` from forcing portrait during recomposition.
    /// - `.all`: Set when the device is in portrait. Allows free rotation in all
    ///   directions.
    static var orientationLock: UIInterfaceOrientationMask = .all

    private var sceneObserver: NSObjectProtocol?
    private var didSetupCompose = false

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions _: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        Logger.logInfo("AnanAppDelegate") { "Launched" }

        // Begin observing physical device orientation to manage the orientation lock.
        // This must be registered early (before Compose starts) so the lock is always
        // set before any Compose recomposition triggers a forced portrait request.
        UIDevice.current.beginGeneratingDeviceOrientationNotifications()

        // Set the initial lock to match the current device orientation. Notification
        // observers only fire on *changes*, so without this the lock would stay `.all`
        // if the app launches while the device is already in landscape.
        if UIDevice.current.orientation.isLandscape {
            Self.orientationLock = .landscape
        }

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(deviceOrientationChanged),
            name: UIDevice.orientationDidChangeNotification,
            object: nil
        )

        // Wait for the scene to activate before replacing the root view controller,
        // since the SwiftUI window doesn't exist until the scene is connected.
        sceneObserver = NotificationCenter.default.addObserver(
            forName: UIScene.didActivateNotification,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            self?.setupCompose(from: notification)
        }

        return true
    }

    /// Replaces the SwiftUI-managed root view controller with `ComposeUIViewController`.
    ///
    /// Called once when the scene first activates. The SwiftUI `WindowGroup` creates a
    /// `UIHostingController` with `Color.clear` as its body; we replace it with the Compose
    /// view controller so that Compose Multiplatform owns the entire view hierarchy.
    private func setupCompose(from notification: Any) {
        guard !didSetupCompose,
              let windowScene = (notification as? Foundation.Notification)?.object as? UIWindowScene,
              let window = windowScene.windows.first
        else { return }
        didSetupCompose = true

        window.rootViewController = MainViewControllerKt.createMainViewController()
    }

    /// Returns the current orientation lock, which the system intersects with the root
    /// view controller's `supportedInterfaceOrientations` to determine allowed rotations.
    func application(
        _ application: UIApplication,
        supportedInterfaceOrientationsFor window: UIWindow?
    ) -> UIInterfaceOrientationMask {
        Self.orientationLock
    }

    /// Updates the orientation lock when the physical device orientation changes.
    ///
    /// Face-up, face-down, and unknown orientations are ignored to avoid unlocking
    /// prematurely (e.g. when the user lays the device flat while in landscape).
    @objc private func deviceOrientationChanged() {
        let device = UIDevice.current.orientation
        if device.isLandscape {
            Self.orientationLock = .landscape
        } else if device.isPortrait {
            Self.orientationLock = .all
        }

        // Tell the system to re-query supported orientations with the updated lock.
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = scene.windows.first?.rootViewController
        {
            rootVC.setNeedsUpdateOfSupportedInterfaceOrientations()
        }
    }
}
