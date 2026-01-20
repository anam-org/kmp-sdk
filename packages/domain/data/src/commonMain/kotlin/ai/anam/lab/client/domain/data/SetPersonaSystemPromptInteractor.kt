package ai.anam.lab.client.domain.data

fun interface SetPersonaSystemPromptInteractor {
    operator fun invoke(systemPrompt: String)
}
