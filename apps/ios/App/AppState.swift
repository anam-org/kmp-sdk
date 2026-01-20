import Shared

class AppState {
    static let shared = AppState()

    fileprivate let appObjectGraph: ClientAppObjectGraph = createApplicationObjectGraph()
}

extension AppState {
    var logger: any Shared.Logger {
        appObjectGraph.logger
    }
}
