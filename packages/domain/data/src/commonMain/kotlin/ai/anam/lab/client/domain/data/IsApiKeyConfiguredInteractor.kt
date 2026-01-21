package ai.anam.lab.client.domain.data

fun interface IsApiKeyConfiguredInteractor {
    suspend operator fun invoke(): Boolean
}
