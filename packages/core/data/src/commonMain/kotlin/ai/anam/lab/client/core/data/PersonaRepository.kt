package ai.anam.lab.client.core.data

import ai.anam.lab.client.core.data.models.Persona
import ai.anam.lab.client.core.logging.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    val current: StateFlow<Persona>
        field = MutableStateFlow(DEFAULT_PERSONA)

    fun withName(name: String) = with { copy(name = name) }
    fun withAvatar(id: String, updatedName: String? = null) = with { copy(avatarId = id, name = updatedName ?: name) }
    fun withVoice(id: String) = with { copy(voiceId = id) }
    fun withLlm(id: String) = with { copy(llmId = id) }
    fun withSystemPrompt(prompt: String) = with { copy(systemPrompt = prompt) }
    fun withMaxSessionLengthSeconds(seconds: Int) = with { copy(maxSessionLengthSeconds = seconds) }

    fun reset() {
        current.value = DEFAULT_PERSONA
        logger.i(TAG) { "Persona reset to defaults" }
    }

    private fun with(block: Persona.() -> Persona) {
        current.value = current.value.block().also {
            logger.i(TAG) { "Updated Persona: $it" }
        }
    }

    private companion object {
        const val TAG = "PersonaRepository"

        val DEFAULT_PERSONA = Persona(
            name = "Liv",
            avatarId = "071b0286-4cce-4808-bee2-e642f1062de3",
            voiceId = "de23e340-1416-4dd8-977d-065a7ca11697",
            llmId = "85906141-db1c-4927-b74d-3c82ebe2436e",
            systemPrompt =
            "You are a helpful, concise, and reliable assistant. You are approachable, professional, and focused on " +
                "providing accurate information in a friendly manner.",
            maxSessionLengthSeconds = 600,
        )
    }
}
