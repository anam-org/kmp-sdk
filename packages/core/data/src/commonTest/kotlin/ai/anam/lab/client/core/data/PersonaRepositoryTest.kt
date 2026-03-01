package ai.anam.lab.client.core.data

import ai.anam.lab.client.core.test.FakeLogger
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class PersonaRepositoryTest {

    @Test
    fun `current emits default persona initially`() = runTest {
        val repo = createRepository()
        val defaultPersona = repo.current.value

        repo.current.test {
            assertEquals(defaultPersona, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withName updates persona name`() = runTest {
        val repo = createRepository()

        repo.withName("Alice")

        repo.current.test {
            assertEquals("Alice", awaitItem().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withAvatar updates avatar id`() = runTest {
        val repo = createRepository()

        repo.withAvatar("new-avatar-id")

        repo.current.test {
            assertEquals("new-avatar-id", awaitItem().avatarId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withAvatar updates name when provided`() = runTest {
        val repo = createRepository()

        repo.withAvatar("new-avatar-id", updatedName = "Updated Name")

        repo.current.test {
            val persona = awaitItem()
            assertEquals("new-avatar-id", persona.avatarId)
            assertEquals("Updated Name", persona.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withVoice updates voice id`() = runTest {
        val repo = createRepository()

        repo.withVoice("new-voice-id")

        repo.current.test {
            assertEquals("new-voice-id", awaitItem().voiceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withLlm updates llm id`() = runTest {
        val repo = createRepository()

        repo.withLlm("new-llm-id")

        repo.current.test {
            assertEquals("new-llm-id", awaitItem().llmId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withSystemPrompt updates system prompt`() = runTest {
        val repo = createRepository()

        repo.withSystemPrompt("Be concise.")

        repo.current.test {
            assertEquals("Be concise.", awaitItem().systemPrompt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `withMaxSessionLengthSeconds updates max session length`() = runTest {
        val repo = createRepository()

        repo.withMaxSessionLengthSeconds(120)

        repo.current.test {
            assertEquals(120, awaitItem().maxSessionLengthSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset restores default persona`() = runTest {
        val repo = createRepository()
        val defaultPersona = repo.current.value

        repo.withName("Modified")
        repo.withVoice("modified-voice")
        repo.reset()

        repo.current.test {
            assertEquals(defaultPersona, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `current emits updates as flow`() = runTest {
        val repo = createRepository()
        val defaultName = repo.current.value.name

        repo.current.test {
            assertEquals(defaultName, awaitItem().name)

            repo.withName("Alice")
            assertEquals("Alice", awaitItem().name)

            repo.withName("Bob")
            assertEquals("Bob", awaitItem().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createRepository(): PersonaRepository {
        return PersonaRepository(logger = FakeLogger())
    }
}
