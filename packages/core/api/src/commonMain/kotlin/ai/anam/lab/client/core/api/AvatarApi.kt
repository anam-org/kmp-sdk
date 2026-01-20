package ai.anam.lab.client.core.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface AvatarApi {
    /**
     * Returns an avatar by ID.
     */
    @GET("v1/avatars/{id}")
    suspend fun getAvatar(@Path("id") id: String): Avatar

    /**
     * Returns all avatars associated with the query.
     */
    @GET("v1/avatars")
    suspend fun getAvatars(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
        @Query("search") query: String? = null,
        @Query("onlyOneShot") onlyOneShot: Boolean? = null,
    ): PagedList<Avatar>
}

@Serializable
data class Avatar(
    @SerialName("id")
    val id: String,

    @SerialName("displayName")
    val displayName: String,

    @SerialName("variantName")
    val variantName: String,

    @SerialName("imageUrl")
    val imageUrl: String,

    @SerialName("videoUrl")
    val videoUrl: String,

    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("updatedAt")
    val updatedAt: Instant,

    @SerialName("createdByOrganizationId")
    val createdByOrganizationId: String? = null,
)
