package ai.anam.lab.client.core.data.models

import ai.anam.lab.client.core.api.PagedList as ApiPagedList

/**
 * This class represents a pageable list of items. This matches our expected API response, but is safer to return from
 * Repositories.
 */
data class PagedList<T>(val data: List<T>, val meta: Meta)

/**
 * The metadata associated with the paged list.
 */
data class Meta(
    val total: Int,
    val lastPage: Int? = null,
    val currentPage: Int,
    val perPage: Int,
    val prev: Int? = null,
    val next: Int? = null,
)

/**
 * Extension function to helper determine if this [Meta] is the last page.
 */
fun Meta.isLastPage(): Boolean = lastPage == null || currentPage == lastPage

/**
 * Converts an [ApiPagedList] to a [PagedList], transforming the data items using the provided [transform] function.
 */
fun <T, U> ApiPagedList<T>.toPagedList(transform: (T) -> U): PagedList<U> {
    return PagedList(
        data = data.map(transform),
        meta = Meta(
            total = meta.total,
            lastPage = meta.lastPage,
            currentPage = meta.currentPage,
            perPage = meta.perPage,
            prev = meta.prev,
            next = meta.next,
        ),
    )
}
