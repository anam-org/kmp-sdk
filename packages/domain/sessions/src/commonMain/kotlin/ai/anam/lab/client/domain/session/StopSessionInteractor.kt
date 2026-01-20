package ai.anam.lab.client.domain.session

import ai.anam.lab.client.core.client.SessionState

fun interface StopSessionInteractor {
    suspend operator fun invoke(): SessionState
}
