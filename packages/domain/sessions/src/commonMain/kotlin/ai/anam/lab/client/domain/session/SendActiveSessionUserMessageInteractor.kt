package ai.anam.lab.client.domain.session

fun interface SendActiveSessionUserMessageInteractor {
    suspend operator fun invoke(content: String)
}
