package ai.anam.lab.client.domain.data

fun interface SetPersonaVoiceInteractor {
    operator fun invoke(voiceId: String)
}
