import SwiftUI

/// SwiftUI entry point that delegates all UI to Compose Multiplatform.
///
/// The `WindowGroup` body is intentionally `Color.clear` — the actual UI is rendered by
/// `ComposeUIViewController`, which `AnanAppDelegate` installs as the window's root view
/// controller once the scene activates. SwiftUI is used solely for app lifecycle and the
/// `UIApplicationDelegateAdaptor` that provides the orientation lock mechanism.
@main
struct AnanApp: App {
    @UIApplicationDelegateAdaptor(AnanAppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            Color.clear.ignoresSafeArea()
        }
    }
}
