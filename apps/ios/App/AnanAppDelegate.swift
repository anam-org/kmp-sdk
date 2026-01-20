import SwiftUI

class AnanAppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions _:
    [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        Logger.logInfo("AnanAppDelegate") { "Launched" }
        return true
    }
}
