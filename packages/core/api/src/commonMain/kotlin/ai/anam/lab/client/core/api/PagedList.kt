package ai.anam.lab.client.core.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * This class represents a paged response from our API server, that contains a number of items. It's possible that more
 * results are available, and this is defined within the [meta] field.
 */
@Serializable
data class PagedList<T>(
    @SerialName("data")
    val data: List<T>,

    @SerialName("meta")
    val meta: Meta,
)

/**
 * The metadata associated with the [PagedList].
 */
@Serializable
data class Meta
@OptIn(ExperimentalSerializationApi::class)
constructor(
    @SerialName("total")
    val total: Int,

    @SerialName("lastPage")
    val lastPage: Int? = null,

    @JsonNames("currentPage", "page")
    val currentPage: Int,

    @SerialName("perPage")
    val perPage: Int,

    @SerialName("prev")
    val prev: Int? = null,

    @SerialName("next")
    val next: Int? = null,
)
