package ai.anam.lab.client.core.data

import ai.anam.lab.client.core.data.models.Persona
import ai.anam.lab.client.core.logging.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * This repository is responsible for managing the current (active) [Persona]. This can be modified before a Session is
 * started which will control the persona used for that session.
 */
@Inject
@SingleIn(AppScope::class)
class PersonaRepository(private val logger: Logger) {

    /**
     * A flow that represents the currently selected [Persona].
     */
    private val _current = MutableStateFlow(DEFAULT_PERSONA)
    val current: Flow<Persona> = _current.asStateFlow()

    fun withName(name: String) = with { copy(name = name) }
    fun withAvatar(id: String, updatedName: String? = null) = with { copy(avatarId = id, name = updatedName ?: name) }
    fun withVoice(id: String) = with { copy(voiceId = id) }
    fun withLlm(id: String) = with { copy(llmId = id) }
    fun withSystemPrompt(prompt: String) = with { copy(systemPrompt = prompt) }
    fun withMaxSessionLengthSeconds(seconds: Int) = with { copy(maxSessionLengthSeconds = seconds) }

    private fun with(block: Persona.() -> Persona) {
        _current.value = _current.value.block().also {
            logger.i(TAG) { "Updated Persona: $it" }
        }
    }

    private companion object {
        const val TAG = "PersonaRepository"

        val DEFAULT_PERSONA = Persona(
            name = "Cara",
            avatarId = "30fa96d0-26c4-4e55-94a0-517025942e18",
            voiceId = "6bfbe25a-979d-40f3-a92b-5394170af54b",
            llmId = "0934d97d-0c3a-4f33-91b0-5e136a0ef466",
            systemPrompt =
            "You are Cara, a helpful customer service representative. Be friendly and " +
                "concise in your responses.",
            maxSessionLengthSeconds = 600,
        )
    }
}
