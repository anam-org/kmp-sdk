package ai.anam.lab.client.feature.settings

import ai.anam.lab.client.core.test.FakeAnamPreferences
import ai.anam.lab.client.core.test.FakeLogger
import ai.anam.lab.client.core.test.FakeNavigator
import ai.anam.lab.client.domain.data.GetApiKeyInteractor
import ai.anam.lab.client.domain.data.UpdateApiKeyInteractor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `initial state has null display key when no key configured`() {
        val viewModel = createViewModel(currentApiKey = null)

        assertNull(viewModel.state.value.displayApiKey)
    }

    @Test
    fun `initial state loads masked display key for long key`() {
        // Keys longer than 8 chars should be truncated with ellipsis
        val viewModel = createViewModel(currentApiKey = "abcdefghijklmnop")

        assertEquals("abcdefgh...", viewModel.state.value.displayApiKey)
    }

    @Test
    fun `initial state shows full key when short`() {
        val viewModel = createViewModel(currentApiKey = "abc")

        assertEquals("abc", viewModel.state.value.displayApiKey)
    }

    @Test
    fun `initial state shows full key at exactly 8 chars`() {
        val viewModel = createViewModel(currentApiKey = "12345678")

        assertEquals("12345678", viewModel.state.value.displayApiKey)
    }

    @Test
    fun `showApiKeyDialog sets dialog visible`() {
        val viewModel = createViewModel()

        viewModel.showApiKeyDialog()

        assertTrue(viewModel.state.value.showApiKeyDialog)
    }

    @Test
    fun `showApiKeyDialog populates apiKey from stored key`() {
        val viewModel = createViewModel(currentApiKey = "my-secret-key")

        viewModel.showApiKeyDialog()

        assertEquals("my-secret-key", viewModel.state.value.apiKey)
    }

    @Test
    fun `showApiKeyDialog populates empty apiKey when no key configured`() {
        val viewModel = createViewModel(currentApiKey = null)

        viewModel.showApiKeyDialog()

        assertEquals("", viewModel.state.value.apiKey)
    }

    @Test
    fun `dismissApiKeyDialog hides dialog`() {
        val viewModel = createViewModel()
        viewModel.showApiKeyDialog()

        viewModel.dismissApiKeyDialog()

        assertFalse(viewModel.state.value.showApiKeyDialog)
    }

    @Test
    fun `saveApiKey closes dialog`() = testScope.runTest {
        val viewModel = createViewModel(currentApiKey = null)
        viewModel.showApiKeyDialog()
        assertTrue(viewModel.state.value.showApiKeyDialog)

        viewModel.saveApiKey("new-key")
        runCurrent()

        assertFalse(viewModel.state.value.showApiKeyDialog)
    }

    @Test
    fun `saveApiKey updates display key to masked value`() = testScope.runTest {
        val viewModel = createViewModel(currentApiKey = null)
        assertNull(viewModel.state.value.displayApiKey)

        viewModel.saveApiKey("abcdefghijklmnop")
        runCurrent()

        assertEquals("abcdefgh...", viewModel.state.value.displayApiKey)
    }

    @Test
    fun `saveApiKey with empty string clears display key`() = testScope.runTest {
        val viewModel = createViewModel(currentApiKey = "existing-key")
        assertEquals("existing...", viewModel.state.value.displayApiKey)

        // Clearing the key should reset the display
        viewModel.saveApiKey("")
        runCurrent()

        assertNull(viewModel.state.value.displayApiKey)
    }

    @Test
    fun `display key formatting null when no key`() {
        val viewModel = createViewModel(currentApiKey = null)

        assertNull(viewModel.state.value.displayApiKey)
    }

    @Test
    fun `display key formatting null for empty key`() {
        val viewModel = createViewModel(currentApiKey = "")

        assertNull(viewModel.state.value.displayApiKey)
    }

    private fun createViewModel(currentApiKey: String? = null): SettingsViewModel {
        var apiKey = currentApiKey
        return SettingsViewModel(
            preferences = FakeAnamPreferences(),
            logger = FakeLogger(),
            navigator = FakeNavigator(),
            updateApiKeyInteractor = UpdateApiKeyInteractor { key ->
                apiKey = key.trim().ifEmpty { null }
                true
            },
            getApiKeyInteractor = GetApiKeyInteractor { apiKey },
        )
    }
}
