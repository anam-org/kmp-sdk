import Shared

func createApplicationObjectGraph() -> any ClientAppObjectGraph {
    let graph = ClientAppObjectGraphKt.createClientAppObjectGraph()

    /// After the ObjectGraph has been created, we can set any iOS specific implementations to it.

    return graph
}
