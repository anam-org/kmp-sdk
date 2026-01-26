package ai.anam.lab.client

import ai.anam.lab.client.core.ClientAppObjectGraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.logging.Logger
import android.app.Application
import dev.zacsweers.metro.createGraphFactory

class AnamApplication : Application() {
    private lateinit var graph: ClientAppObjectGraph
    private val logger: Logger
        get() = graph.logger

    override fun onCreate() {
        super.onCreate()

        // Create the Application's ObjectGraph.
        graph = createGraphFactory<ClientAppObjectGraph.Factory>()
            .create(this)
            .also { ApplicationObjectGraphHolder.set(it) }

        logger.i(TAG) { "Application created..." }
    }

    private companion object {
        const val TAG = "AnamApplication"
    }
}
