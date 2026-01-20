package ai.anam.lab.client.domain.data

fun interface SetPersonaLlmInteractor {
    operator fun invoke(llmId: String)
}
