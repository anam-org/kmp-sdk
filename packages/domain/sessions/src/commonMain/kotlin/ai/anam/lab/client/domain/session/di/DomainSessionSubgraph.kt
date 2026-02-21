package ai.anam.lab.client.domain.session.di

import ai.anam.lab.SessionEvent
import ai.anam.lab.client.core.client.SessionRepository
import ai.anam.lab.client.domain.data.ObserveCurrentPersonaInteractor
import ai.anam.lab.client.domain.session.ObserveActiveMessageHistoryInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionClosedInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionMuteStateInteractor
import ai.anam.lab.client.domain.session.ObserveActiveToolEventsInteractor
import ai.anam.lab.client.domain.session.ObserveIsSessionActiveInteractor
import ai.anam.lab.client.domain.session.SendActiveSessionUserMessageInteractor
import ai.anam.lab.client.domain.session.StartSessionInteractor
import ai.anam.lab.client.domain.session.StartSessionWithCurrentPersonaInteractor
import ai.anam.lab.client.domain.session.StopSessionInteractor
import ai.anam.lab.client.domain.session.ToggleActiveSessionMuteStateInteractor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@ContributesTo(AppScope::class)
interface DomainSessionSubgraph {
    @Provides
    fun providesObserveActiveSessionInteractor(repository: SessionRepository): ObserveActiveSessionInteractor {
        return ObserveActiveSessionInteractor { repository.activeSession }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    fun providesObserveActiveMessageHistoryInteractor(
        session: ObserveActiveSessionInteractor,
    ): ObserveActiveMessageHistoryInteractor {
        return ObserveActiveMessageHistoryInteractor {
            session().flatMapLatest { session ->
                session?.messages ?: flow { emit(emptyList()) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    fun providesObserveActiveToolEventsInteractor(
        session: ObserveActiveSessionInteractor,
    ): ObserveActiveToolEventsInteractor {
        return ObserveActiveToolEventsInteractor {
            session().flatMapLatest { session ->
                session?.toolEvents ?: emptyFlow()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    fun providesObserveActiveSessionClosedInteractor(
        session: ObserveActiveSessionInteractor,
    ): ObserveActiveSessionClosedInteractor {
        return ObserveActiveSessionClosedInteractor {
            session().flatMapLatest { session ->
                session?.events
                    ?.filterIsInstance<SessionEvent.ConnectionClosed>()
                    ?.map { it.reason } ?: emptyFlow()
            }
        }
    }

    @Provides
    fun providesStartSessionInteractor(repository: SessionRepository): StartSessionInteractor {
        return StartSessionInteractor { persona -> repository.startSession(persona) }
    }

    @Provides
    fun providesStartSessionWithCurrentPersonaInteractor(
        observeCurrentPersonaInteractor: ObserveCurrentPersonaInteractor,
        repository: SessionRepository,
    ): StartSessionWithCurrentPersonaInteractor {
        return StartSessionWithCurrentPersonaInteractor {
            repository.startSession(observeCurrentPersonaInteractor().first())
        }
    }

    @Provides
    fun providesStopSessionInteractor(repository: SessionRepository): StopSessionInteractor {
        return StopSessionInteractor { repository.stopSession() }
    }

    @Provides
    fun providesObserveIsSessionActiveInteractor(
        observeActiveSessionInteractor: ObserveActiveSessionInteractor,
    ): ObserveIsSessionActiveInteractor = ObserveIsSessionActiveInteractor {
        observeActiveSessionInteractor().map { session -> session != null }
    }

    @Provides
    fun providesObserveActiveSessionMuteStateInteractor(
        repository: SessionRepository,
    ): ObserveActiveSessionMuteStateInteractor = ObserveActiveSessionMuteStateInteractor {
        repository.isAudioMute
    }

    @Provides
    fun proviesToggleActiveSessionMuteStateInteractor(
        repository: SessionRepository,
    ): ToggleActiveSessionMuteStateInteractor = ToggleActiveSessionMuteStateInteractor {
        repository.toggleAudioMute()
    }

    @Provides
    fun providesSendActiveSessionUserMessageInteractor(
        repository: SessionRepository,
    ): SendActiveSessionUserMessageInteractor = SendActiveSessionUserMessageInteractor { content ->
        repository.sendUserMessage(content)
    }
}
