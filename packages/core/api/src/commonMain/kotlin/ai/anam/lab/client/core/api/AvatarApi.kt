package ai.anam.lab.client.core.api

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.ByteReadChannel
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
     * Deletes an avatar by ID.
     */
    @DELETE("v1/avatars/{id}")
    suspend fun deleteAvatar(@Path("id") id: String)

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

/**
 * Creates a one-shot avatar from an image. This uses [submitFormWithBinaryData] directly rather than Ktorfit because
 * the multipart parts must be constructed manually — Ktor's [formData] builder drops the `filename` parameter from
 * `Content-Disposition`, which the API requires.
 */
suspend fun AvatarApi.createAvatar(
    httpClient: HttpClient,
    displayName: String,
    imageData: ByteArray,
    timeoutMs: Long,
): Avatar {
    val parts = listOf(
        PartData.FormItem(
            value = displayName,
            dispose = {},
            partHeaders = Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"displayName\"")
            },
        ),
        PartData.BinaryChannelItem(
            provider = { ByteReadChannel(imageData) },
            partHeaders = Headers.build {
                append(
                    HttpHeaders.ContentDisposition,
                    "form-data; name=\"imageFile\"; filename=\"avatar.jpg\"",
                )
                append(HttpHeaders.ContentType, "image/jpeg")
            },
        ),
    )
    return httpClient.submitFormWithBinaryData(
        url = "v1/avatars",
        formData = parts,
    ) {
        timeout {
            requestTimeoutMillis = timeoutMs
            socketTimeoutMillis = timeoutMs
        }
    }.body<Avatar>()
}
