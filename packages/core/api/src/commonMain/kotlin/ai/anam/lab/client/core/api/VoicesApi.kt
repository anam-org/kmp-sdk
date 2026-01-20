package ai.anam.lab.client.core.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface VoicesApi {
    /**
     * Returns an voice by ID.
     */
    @GET("v1/voices/{id}")
    suspend fun getVoice(@Path("id") id: String): Voice

    /**
     * Returns all voices associated with the query.
     */
    @GET("v1/voices")
    suspend fun getVoices(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
        @Query("search") query: String? = null,
    ): PagedList<Voice>
}

@Serializable
data class Voice(
    @SerialName("id")
    val id: String,

    @SerialName("displayName")
    val displayName: String,

    @SerialName("provider")
    val provider: String,

    @SerialName("providerVoiceId")
    val providerVoiceId: String,

    @SerialName("providerModelId")
    val providerModelId: String,

    @SerialName("sampleUrl")
    val sampleUrl: String? = null,

    @SerialName("gender")
    val gender: String? = null,

    @SerialName("country")
    val country: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("updatedAt")
    val updatedAt: Instant,

    @SerialName("createdByOrganizationId")
    val createdByOrganizationId: String? = null,
)
