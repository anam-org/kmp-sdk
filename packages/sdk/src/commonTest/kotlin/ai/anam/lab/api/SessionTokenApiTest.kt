package ai.anam.lab.api

import assertk.assertThat
import assertk.assertions.contains
import kotlin.test.Test
import kotlinx.serialization.encodeToString

class SessionTokenApiTest {

    private val json = defaultJsonConfiguration

    @Test
    fun `SessionTokenBody serializes with maxSessionLengthSeconds`() {
        val body = SessionTokenBody(
            personaConfig = PersonaConfig(
                name = "Test",
                avatarId = "avatar-1",
                voiceId = "voice-1",
                llmId = "llm-1",
                systemPrompt = "You are helpful.",
                maxSessionLengthSeconds = 600,
            ),
        )
        val encoded = json.encodeToString(SessionTokenBody.serializer(), body)
        assertThat(encoded).contains("maxSessionLengthSeconds")
        assertThat(encoded).contains("600")
    }
}
