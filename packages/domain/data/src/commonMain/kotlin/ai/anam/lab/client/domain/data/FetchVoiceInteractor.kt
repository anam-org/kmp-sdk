package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.data.models.VoiceErrorReason

fun interface FetchVoiceInteractor {
    suspend operator fun invoke(id: String): Either<VoiceErrorReason, Voice>
}
