package ai.anam.lab.client.domain.data

import kotlinx.coroutines.flow.Flow

/**
 * Returns a [Flow] that emits [Unit] each time the API key is changed. New subscribers will not
 * receive past emissions.
 */
fun interface ObserveApiKeyChangedInteractor {
    operator fun invoke(): Flow<Unit>
}
