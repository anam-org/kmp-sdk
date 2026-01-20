package ai.anam.lab.client.domain.data

fun interface SetPersonaNameInteractor {
    operator fun invoke(name: String)
}
