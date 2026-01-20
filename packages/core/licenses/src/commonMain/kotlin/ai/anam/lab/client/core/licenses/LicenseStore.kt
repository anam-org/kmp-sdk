package ai.anam.lab.client.core.licenses

import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json

@Inject
class LicenseStore(private val resources: Res = Res, private val logger: Logger) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun getLicenses(): List<LicenseItem> {
        val bytes = resources.readBytes("files/licenses.json")
        val string = bytes.decodeToString()
        return json.decodeFromString<List<LicenseItem>>(string).also {
            logger.i(TAG) { "Found ${it.size} licenses..." }
        }
    }

    private companion object {
        const val TAG = "LicenseStore"
    }
}
