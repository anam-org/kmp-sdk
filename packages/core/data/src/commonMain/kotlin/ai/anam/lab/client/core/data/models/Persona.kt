package ai.anam.lab.client.core.data.models

/**
 * Represents a single Persona instance.
 */
data class Persona(
    val name: String,
    val avatarId: String,
    val voiceId: String,
    val llmId: String?,
    val systemPrompt: String,
)
