package ai.anam.lab.client.core.auth

import ai.anam.lab.client.core.test.FakeAnamPreferences
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AuthRepositoryTest {

    @Test
    fun `getApiToken returns stored preference when available`() = runTest {
        val preferences = FakeAnamPreferences()
        preferences.apiKey.set("stored-key-123")

        val repo = createRepository(preferences)

        assertEquals("stored-key-123", repo.getApiToken())
    }

    @Test
    fun `setApiKey returns true on change`() = runTest {
        val repo = createRepository()
        repo.setApiKey("initial-key")

        val result = repo.setApiKey("different-key")

        assertTrue(result)
    }

    @Test
    fun `setApiKey returns false when same key`() = runTest {
        val repo = createRepository()
        repo.setApiKey("test-key")

        // Setting the same key again should report no change
        val result = repo.setApiKey("test-key")

        assertFalse(result)
    }

    @Test
    fun `setApiKey updates what getApiToken returns`() = runTest {
        val repo = createRepository()

        repo.setApiKey("my-new-key")

        assertEquals("my-new-key", repo.getApiToken())
    }

    @Test
    fun `setApiKey trims whitespace`() = runTest {
        val repo = createRepository()

        repo.setApiKey("  trimmed-key  ")

        assertEquals("trimmed-key", repo.getApiToken())
    }

    @Test
    fun `setApiKey with empty string clears token`() = runTest {
        val repo = createRepository()

        // Set a key, then clear it with an empty string
        repo.setApiKey("some-key")
        assertEquals("some-key", repo.getApiToken())

        repo.setApiKey("")
        assertNull(repo.getApiToken())
    }

    @Test
    fun `apiKeyChanged emits on change`() = runTest {
        val repo = createRepository()

        repo.apiKeyChanged.test {
            repo.setApiKey("key-1")
            awaitItem()

            repo.setApiKey("key-2")
            awaitItem()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `apiKeyChanged does not emit when same key`() = runTest {
        val repo = createRepository()

        repo.apiKeyChanged.test {
            // First call changes from initial value
            repo.setApiKey("same-key")
            awaitItem()

            // Second call with same value should not emit
            repo.setApiKey("same-key")
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createRepository(preferences: FakeAnamPreferences = FakeAnamPreferences()): AuthRepository {
        return AuthRepository(preferences = preferences)
    }
}
