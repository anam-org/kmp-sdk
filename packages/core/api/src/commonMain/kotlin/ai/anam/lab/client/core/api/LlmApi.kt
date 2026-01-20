package ai.anam.lab.client.core.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface LlmApi {
    /**
     * Returns an LLM by ID.
     */
    @GET("v1/llms/{id}")
    suspend fun getLlm(@Path("id") id: String): Llm

    /**
     * Returns all LLMs associated with the query.
     */
    @GET("v1/llms")
    suspend fun getLlms(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
        @Query("search") query: String? = null,
        @Query("includeDefaults") includeDefaults: Boolean? = null,
    ): PagedList<Llm>
}

@Serializable
data class Llm(
    @SerialName("id")
    val id: String,

    @SerialName("displayName")
    val displayName: String,

    @SerialName("description")
    val description: String?,

    @SerialName("llmFormat")
    val llmFormat: String,

    @SerialName("modelName")
    val modelName: String?,

    @SerialName("temperature")
    val temperature: Double?,

    @SerialName("maxTokens")
    val maxTokens: Int?,

    @SerialName("deploymentName")
    val deploymentName: String?,

    @SerialName("apiVersion")
    val apiVersion: String?,

    @SerialName("displayTags")
    val displayTags: List<String>,

    @SerialName("isDefault")
    val isDefault: Boolean,

    @SerialName("isGlobal")
    val isGlobal: Boolean,

    @SerialName("createdByOrganizationId")
    val createdByOrganizationId: String?,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String?,
)
