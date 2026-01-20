package ai.anam.lab.client.domain.data

fun interface SetPersonaAvatarInteractor {
    operator fun invoke(avatarId: String, name: String?)
}
