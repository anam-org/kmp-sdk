package ai.anam.lab.client.feature.llms

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.data.models.LlmErrorReason
import ai.anam.lab.client.core.data.models.Meta
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.test.FakeLogger
import ai.anam.lab.client.domain.data.FetchLlmsInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentLlmIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaLlmInteractor
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
class LlmsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // region Init / Observation

    @Test
    fun `selectedId updates when observed llm id changes`() = testScope.runTest {
        val llmIdFlow = MutableSharedFlow<String?>()
        val viewModel = createViewModel(currentLlmIdFlow = llmIdFlow)

        viewModel.state.test {
            assertThat(awaitItem().selectedId).isNull()

            llmIdFlow.emit("llm-1")

            assertThat(awaitItem().selectedId).isEqualTo("llm-1")
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

    // region LLM Selection

    @Test
    fun `setLlm updates selectedId and calls interactor`() = testScope.runTest {
        var setLlmId: String? = null
        val viewModel = createViewModel(
            onSetPersonaLlm = { id -> setLlmId = id },
        )

        viewModel.state.test {
            assertThat(awaitItem().selectedId).isNull()

            viewModel.setLlm("llm-1")
            runCurrent()

            assertThat(awaitItem().selectedId).isEqualTo("llm-1")
            assertThat(setLlmId).isEqualTo("llm-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Helpers

    private fun createViewModel(
        fetchLlms: suspend (Int, Int, String?, Boolean?) -> Either<LlmErrorReason, PagedList<Llm>> =
            { _, _, _, _ -> Either.Right(testPage()) },
        currentLlmIdFlow: Flow<String?> = emptyFlow(),
        apiKeyChangedFlow: Flow<Unit> = emptyFlow(),
        onSetPersonaLlm: (String) -> Unit = {},
    ): LlmsViewModel = LlmsViewModel(
        fetchLlmsInteractor = FetchLlmsInteractor { page, perPage, query, includeDefaults ->
            fetchLlms(page, perPage, query, includeDefaults)
        },
        observeCurrentLlmIdInteractor = ObserveCurrentLlmIdInteractor { currentLlmIdFlow },
        setPersonaLlmInteractor = SetPersonaLlmInteractor { id -> onSetPersonaLlm(id) },
        observeApiKeyChangedInteractor = ObserveApiKeyChangedInteractor { apiKeyChangedFlow },
        logger = FakeLogger(),
    ).also { testScope.runCurrent() }

    private fun testLlm(id: String = "llm-1") = Llm(
        id = id,
        displayName = "Test LLM $id",
        description = null,
        llmFormat = "openai",
        modelName = "gpt-4",
        temperature = null,
        maxTokens = null,
        deploymentName = null,
        apiVersion = null,
        displayTags = emptyList(),
        isDefault = false,
        isGlobal = false,
        createdByOrganizationId = null,
        createdAt = Instant.fromEpochSeconds(0),
        updatedAt = null,
    )

    private fun testPage(llms: List<Llm> = listOf(testLlm()), currentPage: Int = 1, lastPage: Int = 1) = PagedList(
        data = llms,
        meta = Meta(
            total = llms.size,
            lastPage = lastPage,
            currentPage = currentPage,
            perPage = 10,
        ),
    )

    // endregion
}
