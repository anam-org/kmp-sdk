package ai.anam.lab.client.feature.avatars

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason
import ai.anam.lab.client.core.data.models.Meta
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.notifications.ErrorCode
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.core.test.FakeLogger
import ai.anam.lab.client.domain.data.DeleteAvatarInteractor
import ai.anam.lab.client.domain.data.FetchAvatarsInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaAvatarInteractor
import ai.anam.lab.client.domain.notifications.SendNotificationInteractor
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // region Init / Observation

    @Test
    fun `selectedId updates when observed avatar id changes`() = testScope.runTest {
        val avatarIdFlow = MutableSharedFlow<String?>()
        val viewModel = createViewModel(currentAvatarIdFlow = avatarIdFlow)

        viewModel.state.test {
            assertThat(awaitItem().selectedId).isNull()

            avatarIdFlow.emit("avatar-1")

            assertThat(awaitItem().selectedId).isEqualTo("avatar-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `api key change resets pagination`() = testScope.runTest {
        val apiKeyChanged = MutableSharedFlow<Unit>()
        val viewModel = createViewModel(apiKeyChangedFlow = apiKeyChanged)

        viewModel.state.test {
            val initialItems = awaitItem().items

            apiKeyChanged.emit(Unit)

            // resetPagination() creates a new PaginationState and updates the state
            assertThat(awaitItem().items).isNotSameInstanceAs(initialItems)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Search & Filters

    @Test
    fun `onQueryChange updates query state`() = testScope.runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(awaitItem().query).isEqualTo("")

            viewModel.onQueryChange("test")

            assertThat(awaitItem().query).isEqualTo("test")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onOneShotChange updates filter state`() = testScope.runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertThat(awaitItem().onlyOneShot).isFalse()

            viewModel.onOneShotChange(true)

            assertThat(awaitItem().onlyOneShot).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetFilters clears query and oneShot`() = testScope.runTest {
        val viewModel = createViewModel()
        viewModel.onQueryChange("test")
        viewModel.onOneShotChange(true)

        viewModel.state.test {
            val current = awaitItem()
            assertThat(current.query).isEqualTo("test")
            assertThat(current.onlyOneShot).isTrue()

            viewModel.resetFilters()

            val updated = awaitItem()
            assertThat(updated.query).isEqualTo("")
            assertThat(updated.onlyOneShot).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Avatar Selection

    @Test
    fun `setAvatar updates selectedId and calls interactor`() = testScope.runTest {
        var setAvatarId: String? = null
        var setAvatarName: String? = null
        val viewModel = createViewModel(
            onSetPersonaAvatar = { id, name ->
                setAvatarId = id
                setAvatarName = name
            },
        )

        viewModel.state.test {
            assertThat(awaitItem().selectedId).isNull()

            viewModel.setAvatar("avatar-1", "My Avatar")
            runCurrent()

            assertThat(awaitItem().selectedId).isEqualTo("avatar-1")
            assertThat(setAvatarId).isEqualTo("avatar-1")
            assertThat(setAvatarName).isEqualTo("My Avatar")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Delete — Confirmation Flow

    @Test
    fun `deleteAvatar sends confirmation notification`() = testScope.runTest {
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            onSendNotification = { lastNotification = it },
        )

        viewModel.deleteAvatar("avatar-1")
        runCurrent()

        assertThat(lastNotification).isNotNull().isInstanceOf<Notification.Confirmation>()
    }

    // endregion

    // region Delete — Success + Re-fetch

    @Test
    fun `successful delete on non-last page re-fetches last loaded page`() = testScope.runTest {
        val fetchCalls = mutableListOf<Pair<Int, Int>>()
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            fetchAvatars = { page, perPage, _, _ ->
                fetchCalls.add(page to perPage)
                Either.Right(testPage(lastPage = 2))
            },
            deleteAvatar = { Either.Right(Unit) },
            onSendNotification = { lastNotification = it },
        )
        runCurrent()
        fetchCalls.clear()

        viewModel.deleteAvatar("avatar-1")
        runCurrent()

        val confirmation = lastNotification as Notification.Confirmation
        confirmation.onConfirm()
        runCurrent()

        // Re-fetch only the last loaded page: page=1, perPage=10
        assertThat(fetchCalls).containsExactly(1 to 10)
    }

    @Test
    fun `successful delete on last page removes item locally without re-fetch`() = testScope.runTest {
        val avatar1 = testAvatar("a1")
        val avatar2 = testAvatar("a2")
        val avatar3 = testAvatar("a3")
        val fetchCalls = mutableListOf<Pair<Int, Int>>()
        var deleteCount = 0
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            fetchAvatars = { page, perPage, _, _ ->
                fetchCalls.add(page to perPage)
                when (deleteCount) {
                    // First delete refetch: returns remaining items, IS last page
                    1 -> Either.Right(testPage(avatars = listOf(avatar2, avatar3)))
                    else -> Either.Right(testPage(avatars = listOf(avatar1, avatar2, avatar3)))
                }
            },
            deleteAvatar = {
                deleteCount++
                Either.Right(Unit)
            },
            onSendNotification = { lastNotification = it },
        )
        runCurrent()
        fetchCalls.clear()

        // First delete: populates allItems via refetch and sets lastIsLastPage=true
        viewModel.deleteAvatar("a1")
        runCurrent()
        (lastNotification as Notification.Confirmation).onConfirm()
        runCurrent()
        fetchCalls.clear()

        // Second delete: allItems is populated and lastIsLastPage=true → local removal only
        viewModel.deleteAvatar("a2")
        runCurrent()
        (lastNotification as Notification.Confirmation).onConfirm()
        runCurrent()

        assertThat(fetchCalls).isEmpty()
        assertThat(viewModel.state.value.items.allItems).isNotNull()
            .containsExactly(avatar3)
    }

    @Test
    fun `successful delete on non-last page appends boundary item`() = testScope.runTest {
        val avatar1 = testAvatar("a1")
        val avatar2 = testAvatar("a2")
        val boundaryAvatar = testAvatar("boundary")
        var deleteCalled = false
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            fetchAvatars = { _, _, _, _ ->
                if (deleteCalled) {
                    Either.Right(testPage(avatars = listOf(avatar2, boundaryAvatar), lastPage = 2))
                } else {
                    Either.Right(testPage(avatars = listOf(avatar1, avatar2), lastPage = 2))
                }
            },
            deleteAvatar = {
                deleteCalled = true
                Either.Right(Unit)
            },
            onSendNotification = { lastNotification = it },
        )
        runCurrent()

        viewModel.deleteAvatar("a1")
        runCurrent()

        val confirmation = lastNotification as Notification.Confirmation
        confirmation.onConfirm()
        runCurrent()

        assertThat(viewModel.state.value.items.allItems).isNotNull()
            .containsExactly(avatar2, boundaryAvatar)
    }

    @Test
    fun `successful delete on non-last page does not duplicate boundary item already in list`() = testScope.runTest {
        val avatar1 = testAvatar("a1")
        val avatar2 = testAvatar("a2")
        var deleteCalled = false
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            fetchAvatars = { _, _, _, _ ->
                if (deleteCalled) {
                    Either.Right(testPage(avatars = listOf(avatar2), lastPage = 2))
                } else {
                    Either.Right(testPage(avatars = listOf(avatar1, avatar2), lastPage = 2))
                }
            },
            deleteAvatar = {
                deleteCalled = true
                Either.Right(Unit)
            },
            onSendNotification = { lastNotification = it },
        )
        runCurrent()

        viewModel.deleteAvatar("a1")
        runCurrent()

        val confirmation = lastNotification as Notification.Confirmation
        confirmation.onConfirm()
        runCurrent()

        assertThat(viewModel.state.value.items.allItems).isNotNull()
            .containsExactly(avatar2)
    }

    @Test
    fun `successful delete updates lastIsLastPage from refetched meta`() = testScope.runTest {
        val avatar1 = testAvatar("a1")
        val avatar2 = testAvatar("a2")
        val fetchCalls = mutableListOf<Pair<Int, Int>>()
        var deleteCount = 0
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            fetchAvatars = { page, perPage, _, _ ->
                fetchCalls.add(page to perPage)
                when (deleteCount) {
                    0 -> Either.Right(testPage(avatars = listOf(avatar1, avatar2), lastPage = 2))
                    1 -> Either.Right(testPage(avatars = listOf(avatar2), lastPage = 1))
                    else -> Either.Right(testPage(avatars = emptyList(), lastPage = 1))
                }
            },
            deleteAvatar = {
                deleteCount++
                Either.Right(Unit)
            },
            onSendNotification = { lastNotification = it },
        )
        runCurrent()
        fetchCalls.clear()

        // First delete: not on last page, triggers refetch
        viewModel.deleteAvatar("a1")
        runCurrent()
        (lastNotification as Notification.Confirmation).onConfirm()
        runCurrent()

        assertThat(fetchCalls).hasSize(1)
        fetchCalls.clear()

        // Second delete: lastIsLastPage should now be true (from refetched meta),
        // so no re-fetch should occur
        viewModel.deleteAvatar("a2")
        runCurrent()
        (lastNotification as Notification.Confirmation).onConfirm()
        runCurrent()

        assertThat(fetchCalls).isEmpty()
    }

    // endregion

    // region Delete — Error Handling

    @Test
    fun `delete api failure sends error notification`() = testScope.runTest {
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            deleteAvatar = { Either.Left(AvatarErrorReason.Unknown("Server error")) },
            onSendNotification = { lastNotification = it },
        )

        viewModel.deleteAvatar("avatar-1")
        runCurrent()

        val confirmation = lastNotification as Notification.Confirmation
        confirmation.onConfirm()
        runCurrent()

        assertThat(lastNotification).isNotNull().isInstanceOf<Notification.Error>()
        val error = lastNotification as Notification.Error
        assertThat(error.errorCode).isEqualTo(ErrorCode.API_ERROR)
    }

    @Test
    fun `delete re-fetch failure falls back to reset`() = testScope.runTest {
        var reFetchShouldFail = false
        var lastNotification: Notification? = null
        val viewModel = createViewModel(
            fetchAvatars = { _, _, _, _ ->
                if (reFetchShouldFail) {
                    Either.Left(AvatarErrorReason.Unknown("Re-fetch failed"))
                } else {
                    Either.Right(testPage(lastPage = 2))
                }
            },
            deleteAvatar = {
                reFetchShouldFail = true
                Either.Right(Unit)
            },
            onSendNotification = { lastNotification = it },
        )
        runCurrent()

        viewModel.state.test {
            val itemsBefore = awaitItem().items

            viewModel.deleteAvatar("avatar-1")
            runCurrent()

            val confirmation = lastNotification as Notification.Confirmation
            confirmation.onConfirm()

            // Re-fetch failure triggers resetPagination() which creates a new PaginationState
            assertThat(awaitItem().items).isNotSameInstanceAs(itemsBefore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Helpers

    private fun createViewModel(
        fetchAvatars: suspend (Int, Int, String?, Boolean?) -> Either<AvatarErrorReason, PagedList<Avatar>> =
            { _, _, _, _ -> Either.Right(testPage()) },
        deleteAvatar: suspend (String) -> Either<AvatarErrorReason, Unit> =
            { Either.Right(Unit) },
        currentAvatarIdFlow: Flow<String?> = emptyFlow(),
        apiKeyChangedFlow: Flow<Unit> = emptyFlow(),
        onSetPersonaAvatar: (String, String?) -> Unit = { _, _ -> },
        onSendNotification: suspend (Notification) -> Unit = {},
    ): AvatarsViewModel = AvatarsViewModel(
        fetchAvatarsInteractor = FetchAvatarsInteractor { page, perPage, query, oneShot ->
            fetchAvatars(page, perPage, query, oneShot)
        },
        deleteAvatarInteractor = DeleteAvatarInteractor { id -> deleteAvatar(id) },
        observeCurrentAvatarIdInteractor = ObserveCurrentAvatarIdInteractor { currentAvatarIdFlow },
        setPersonaAvatarInteractor = SetPersonaAvatarInteractor { id, name ->
            onSetPersonaAvatar(id, name)
        },
        observeApiKeyChangedInteractor = ObserveApiKeyChangedInteractor { apiKeyChangedFlow },
        sendNotificationInteractor = SendNotificationInteractor { notification ->
            onSendNotification(notification)
        },
        logger = FakeLogger(),
    ).also { testScope.runCurrent() }

    private fun testAvatar(id: String = "avatar-1") = Avatar(
        id = id,
        displayName = "Test Avatar $id",
        imageUrl = "https://example.com/$id.png",
        videoUrl = "https://example.com/$id.mp4",
        updatedAt = Instant.fromEpochSeconds(0),
    )

    private fun testPage(avatars: List<Avatar> = listOf(testAvatar()), currentPage: Int = 1, lastPage: Int = 1) =
        PagedList(
            data = avatars,
            meta = Meta(
                total = avatars.size,
                lastPage = lastPage,
                currentPage = currentPage,
                perPage = 10,
            ),
        )

    // endregion
}
