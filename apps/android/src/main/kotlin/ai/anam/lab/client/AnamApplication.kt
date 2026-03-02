package ai.anam.lab.client

import ai.anam.lab.client.core.ClientAppObjectGraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.logging.Logger
import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.zacsweers.metro.createGraphFactory

class AnamApplication : Application() {
    private lateinit var graph: ClientAppObjectGraph
    private val logger: Logger
        get() = graph.logger

    override fun onCreate() {
        super.onCreate()

        // Disable Crashlytics in debug builds to keep the dashboard clean.
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !isDebuggable

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
