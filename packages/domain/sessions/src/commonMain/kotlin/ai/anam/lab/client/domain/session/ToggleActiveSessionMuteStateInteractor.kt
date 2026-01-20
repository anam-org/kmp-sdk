package ai.anam.lab.client.domain.session

fun interface ToggleActiveSessionMuteStateInteractor {
    suspend operator fun invoke()
}
