package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.data.models.Persona
import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentPersonaInteractor {
    suspend operator fun invoke(): Flow<Persona>
}
