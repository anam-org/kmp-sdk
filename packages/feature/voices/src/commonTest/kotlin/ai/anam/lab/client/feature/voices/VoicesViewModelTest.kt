package ai.anam.lab.client.feature.voices

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Meta
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.data.models.VoiceErrorReason
import ai.anam.lab.client.core.test.FakeLogger
import ai.anam.lab.client.domain.data.FetchVoicesInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentVoiceIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaVoiceInteractor
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.isNull
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
class VoicesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // region Init / Observation

    @Test
    fun `selectedId updates when observed voice id changes`() = testScope.runTest {
        val voiceIdFlow = MutableSharedFlow<String?>()
        val viewModel = createViewModel(currentVoiceIdFlow = voiceIdFlow)

        viewModel.state.test {
            assertThat(awaitItem().selectedId).isNull()

            voiceIdFlow.emit("voice-1")

            assertThat(awaitItem().selectedId).isEqualTo("voice-1")
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

            assertThat(awaitItem().items).isNotSameInstanceAs(initialItems)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Search

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

    // endregion

    // region Voice Selection

    @Test
    fun `setVoice updates selectedId and calls interactor`() = testScope.runTest {
        var setVoiceId: String? = null
        val viewModel = createViewModel(
            onSetPersonaVoice = { id -> setVoiceId = id },
        )

        viewModel.state.test {
            assertThat(awaitItem().selectedId).isNull()

            viewModel.setVoice("voice-1")
            runCurrent()

            assertThat(awaitItem().selectedId).isEqualTo("voice-1")
            assertThat(setVoiceId).isEqualTo("voice-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Helpers

    private fun createViewModel(
        fetchVoices: suspend (Int, Int, String?) -> Either<VoiceErrorReason, PagedList<Voice>> =
            { _, _, _ -> Either.Right(testPage()) },
        currentVoiceIdFlow: Flow<String?> = emptyFlow(),
        apiKeyChangedFlow: Flow<Unit> = emptyFlow(),
        onSetPersonaVoice: (String) -> Unit = {},
    ): VoicesViewModel = VoicesViewModel(
        fetchVoicesInteractor = FetchVoicesInteractor { page, perPage, query ->
            fetchVoices(page, perPage, query)
        },
        observeCurrentVoiceIdInteractor = ObserveCurrentVoiceIdInteractor { currentVoiceIdFlow },
        setPersonaVoiceInteractor = SetPersonaVoiceInteractor { id -> onSetPersonaVoice(id) },
        observeApiKeyChangedInteractor = ObserveApiKeyChangedInteractor { apiKeyChangedFlow },
        logger = FakeLogger(),
    ).also { testScope.runCurrent() }

    private fun testVoice(id: String = "voice-1") = Voice(
        id = id,
        displayName = "Test Voice $id",
        provider = "elevenlabs",
        providerVoiceId = "pv-$id",
        providerModelId = "pm-$id",
        sampleUrl = null,
        gender = null,
        country = null,
        description = null,
        createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0),
        createdByOrganizationId = null,
    )

    private fun testPage(voices: List<Voice> = listOf(testVoice()), currentPage: Int = 1, lastPage: Int = 1) =
        PagedList(
            data = voices,
            meta = Meta(
                total = voices.size,
                lastPage = lastPage,
                currentPage = currentPage,
                perPage = 10,
            ),
        )

    // endregion
}
