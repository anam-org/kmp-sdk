import SwiftUI

@main
struct AnanApp: App {
    @UIApplicationDelegateAdaptor(AnanAppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
