package ai.anam.lab.client.feature.home

import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.test.FakeLogger
import ai.anam.lab.client.core.test.FakeNavigator
import ai.anam.lab.client.domain.data.IsApiKeyConfiguredInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.UpdateApiKeyInteractor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `welcome overlay shown when api key not configured`() = testScope.runTest {
        val viewModel = createViewModel(isApiKeyConfigured = { false })

        assertTrue(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `welcome overlay hidden when api key configured`() = testScope.runTest {
        val viewModel = createViewModel(isApiKeyConfigured = { true })

        assertFalse(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `dismissWelcomeOverlay hides overlay`() = testScope.runTest {
        val viewModel = createViewModel(isApiKeyConfigured = { false })
        assertTrue(viewModel.state.value.showWelcomeOverlay)

        viewModel.dismissWelcomeOverlay()

        assertFalse(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `dismissWelcomeOverlay prevents overlay from reappearing`() = testScope.runTest {
        val apiKeyChanged = MutableSharedFlow<Unit>()
        val viewModel = createViewModel(
            isApiKeyConfigured = { false },
            apiKeyChangedFlow = apiKeyChanged,
        )
        assertTrue(viewModel.state.value.showWelcomeOverlay)

        // Dismiss the overlay manually
        viewModel.dismissWelcomeOverlay()
        assertFalse(viewModel.state.value.showWelcomeOverlay)

        // Simulate an API key change — overlay should stay hidden
        apiKeyChanged.emit(Unit)
        runCurrent()
        assertFalse(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `saveApiKey calls updateApiKeyInteractor`() = testScope.runTest {
        var updatedKey: String? = null
        val viewModel = createViewModel(
            isApiKeyConfigured = { false },
            onUpdateApiKey = { key ->
                updatedKey = key
                true
            },
        )

        viewModel.saveApiKey("test-key")
        runCurrent()

        assertEquals("test-key", updatedKey)
    }

    @Test
    fun `saveApiKey dismisses overlay when key changed`() = testScope.runTest {
        val apiKeyChanged = MutableSharedFlow<Unit>()
        var isConfigured = false
        val viewModel = createViewModel(
            isApiKeyConfigured = { isConfigured },
            apiKeyChangedFlow = apiKeyChanged,
            onUpdateApiKey = {
                isConfigured = true
                true
            },
        )
        assertTrue(viewModel.state.value.showWelcomeOverlay)

        // Save a new key, then signal the change
        viewModel.saveApiKey("new-key")
        runCurrent()
        apiKeyChanged.emit(Unit)
        runCurrent()

        assertFalse(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `saveApiKey does not dismiss overlay when key unchanged`() = testScope.runTest {
        val viewModel = createViewModel(
            isApiKeyConfigured = { false },
            onUpdateApiKey = { false },
        )
        assertTrue(viewModel.state.value.showWelcomeOverlay)

        // Saving the same key should not dismiss the overlay
        viewModel.saveApiKey("same-key")
        runCurrent()

        assertTrue(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `selectTab updates selectedIndex`() = testScope.runTest {
        val viewModel = createViewModel()

        viewModel.selectTab(2)

        assertEquals(2, viewModel.state.value.selectedIndex)
    }

    @Test
    fun `api key change auto-hides overlay`() = testScope.runTest {
        val apiKeyChanged = MutableSharedFlow<Unit>()
        var isConfigured = false
        val viewModel = createViewModel(
            isApiKeyConfigured = { isConfigured },
            apiKeyChangedFlow = apiKeyChanged,
        )
        assertTrue(viewModel.state.value.showWelcomeOverlay)

        // Simulate the key becoming configured externally
        isConfigured = true
        apiKeyChanged.emit(Unit)
        runCurrent()

        assertFalse(viewModel.state.value.showWelcomeOverlay)
    }

    @Test
    fun `initial state has default tab selection`() = testScope.runTest {
        val viewModel = createViewModel()

        assertEquals(0, viewModel.state.value.selectedIndex)
        assertEquals(4, viewModel.state.value.tabs.size)
    }

    @Test
    fun `selectSettings navigates to settings`() = testScope.runTest {
        val navigator = FakeNavigator()
        val viewModel = createViewModel(navigator = navigator)

        viewModel.selectSettings()

        assertEquals(FeatureRoute.Settings, navigator.lastRoute)
    }

    private fun createViewModel(
        navigator: FakeNavigator = FakeNavigator(),
        isApiKeyConfigured: () -> Boolean = { true },
        apiKeyChangedFlow: Flow<Unit> = emptyFlow(),
        onUpdateApiKey: suspend (String) -> Boolean = { true },
    ): HomeViewModel = HomeViewModel(
        logger = FakeLogger(),
        navigator = navigator,
        isApiKeyConfiguredInteractor = IsApiKeyConfiguredInteractor { isApiKeyConfigured() },
        updateApiKeyInteractor = UpdateApiKeyInteractor { key -> onUpdateApiKey(key) },
        observeApiKeyChangedInteractor = ObserveApiKeyChangedInteractor { apiKeyChangedFlow },
    ).also { testScope.runCurrent() }
}
