package ai.anam.lab.client.domain.session

import ai.anam.lab.client.core.client.SessionState
import ai.anam.lab.client.core.data.models.Persona

fun interface StartSessionInteractor {
    suspend operator fun invoke(persona: Persona): SessionState
}
