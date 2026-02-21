package ai.anam.lab.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class CollectionsTest {

    @Test
    fun `appends item when id is not found`() {
        val list = listOf(item("1", "a"))
        val result = list.updateOrAppend(
            item = item("2", "b"),
            id = { it.first },
            merge = { _, incoming -> incoming },
        )
        assertThat(result).isEqualTo(listOf(item("1", "a"), item("2", "b")))
    }

    @Test
    fun `appends item to empty list`() {
        val list = emptyList<Pair<String, String>>()
        val result = list.updateOrAppend(
            item = item("1", "a"),
            id = { it.first },
            merge = { _, incoming -> incoming },
        )
        assertThat(result).isEqualTo(listOf(item("1", "a")))
    }

    @Test
    fun `merges item when id matches`() {
        val list = listOf(item("1", "hello"))
        val result = list.updateOrAppend(
            item = item("1", " world"),
            id = { it.first },
            merge = { existing, incoming -> existing.copy(second = existing.second + incoming.second) },
        )
        assertThat(result).isEqualTo(listOf(item("1", "hello world")))
    }

    @Test
    fun `merges correct item when multiple items exist`() {
        val list = listOf(item("1", "a"), item("2", "b"), item("3", "c"))
        val result = list.updateOrAppend(
            item = item("2", "x"),
            id = { it.first },
            merge = { existing, incoming -> existing.copy(second = existing.second + incoming.second) },
        )
        assertThat(result).isEqualTo(listOf(item("1", "a"), item("2", "bx"), item("3", "c")))
    }

    @Test
    fun `does not modify other items when merging`() {
        val list = listOf(item("1", "a"), item("2", "b"))
        val result = list.updateOrAppend(
            item = item("1", "z"),
            id = { it.first },
            merge = { _, incoming -> incoming },
        )
        assertThat(result).isEqualTo(listOf(item("1", "z"), item("2", "b")))
    }

    @Test
    fun `does not mutate original list`() {
        val list = listOf(item("1", "a"), item("2", "b"))
        list.updateOrAppend(
            item = item("1", "z"),
            id = { it.first },
            merge = { _, incoming -> incoming },
        )
        assertThat(list).isEqualTo(listOf(item("1", "a"), item("2", "b")))
    }

    @Test
    fun `merge receives existing and incoming items`() {
        val list = listOf(item("1", "existing"))
        val result = list.updateOrAppend(
            item = item("1", "incoming"),
            id = { it.first },
            merge = { existing, incoming -> item(existing.first, "${existing.second}+${incoming.second}") },
        )
        assertThat(result).isEqualTo(listOf(item("1", "existing+incoming")))
    }

    private fun item(id: String, value: String) = Pair(id, value)
}
