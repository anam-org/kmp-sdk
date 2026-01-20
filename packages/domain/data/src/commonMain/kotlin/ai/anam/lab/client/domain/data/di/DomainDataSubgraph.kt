package ai.anam.lab.client.domain.data.di

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.AvatarRepository
import ai.anam.lab.client.core.data.LlmRepository
import ai.anam.lab.client.core.data.PersonaRepository
import ai.anam.lab.client.core.data.VoiceRepository
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.licenses.LicenseStore
import ai.anam.lab.client.domain.data.FetchAvatarInteractor
import ai.anam.lab.client.domain.data.FetchAvatarsInteractor
import ai.anam.lab.client.domain.data.FetchLicensesInteractor
import ai.anam.lab.client.domain.data.FetchLlmInteractor
import ai.anam.lab.client.domain.data.FetchLlmsInteractor
import ai.anam.lab.client.domain.data.FetchVoiceInteractor
import ai.anam.lab.client.domain.data.FetchVoicesInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarIdInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentLlmIdInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentPersonaInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentVoiceIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaAvatarInteractor
import ai.anam.lab.client.domain.data.SetPersonaLlmInteractor
import ai.anam.lab.client.domain.data.SetPersonaNameInteractor
import ai.anam.lab.client.domain.data.SetPersonaSystemPromptInteractor
import ai.anam.lab.client.domain.data.SetPersonaVoiceInteractor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@ContributesTo(AppScope::class)
interface DomainDataSubgraph {

    @Provides
    fun providesObserveCurrentPersonaInteractor(repo: PersonaRepository): ObserveCurrentPersonaInteractor =
        ObserveCurrentPersonaInteractor { repo.current }

    @Provides
    fun providesObserveCurrentAvatarInteractor(
        observeCurrentPersonaInteractor: ObserveCurrentPersonaInteractor,
        fetchAvatarInteractor: FetchAvatarInteractor,
    ): ObserveCurrentAvatarInteractor = ObserveCurrentAvatarInteractor {
        observeCurrentPersonaInteractor().map { persona -> persona.avatarId }
            .distinctUntilChanged()
            .map { id -> fetchAvatarInteractor(id) }
    }

    @Provides
    fun providesObserveCurrentAvatarIdInteractor(
        observeCurrentAvatarInteractor: ObserveCurrentAvatarInteractor,
    ): ObserveCurrentAvatarIdInteractor = ObserveCurrentAvatarIdInteractor {
        observeCurrentAvatarInteractor().map { result ->
            when (result) {
                is Either.Left<*> -> null
                is Either.Right<Avatar> -> result.value.id
            }
        }
    }

    @Provides
    fun providesObserveCurrentVoiceIdInteractor(
        observeCurrentPersonaInteractor: ObserveCurrentPersonaInteractor,
    ): ObserveCurrentVoiceIdInteractor = ObserveCurrentVoiceIdInteractor {
        observeCurrentPersonaInteractor().map { persona -> persona.voiceId }
    }

    @Provides
    fun providesObserveCurrentLlmIdInteractor(
        observeCurrentPersonaInteractor: ObserveCurrentPersonaInteractor,
    ): ObserveCurrentLlmIdInteractor = ObserveCurrentLlmIdInteractor {
        observeCurrentPersonaInteractor().map { persona -> persona.llmId }
    }

    @Provides
    fun providesFetchAvatarInteractor(repo: AvatarRepository): FetchAvatarInteractor = FetchAvatarInteractor { id ->
        repo.getAvatar(id)
    }

    @Provides
    fun providesFetchAvatarsInteractor(repo: AvatarRepository): FetchAvatarsInteractor = FetchAvatarsInteractor {
            page,
            perPage,
            query,
            onlyOneShot,
        ->
        repo.getAvatars(page, perPage, query, onlyOneShot)
    }

    @Provides
    fun providesFetchVoiceInteractor(repo: VoiceRepository): FetchVoiceInteractor = FetchVoiceInteractor { id ->
        repo.getVoice(id)
    }

    @Provides
    fun providesFetchVoicesInteractor(repo: VoiceRepository): FetchVoicesInteractor = FetchVoicesInteractor {
            page,
            perPage,
            query,
        ->
        repo.getVoices(page, perPage, query)
    }

    @Provides
    fun providesFetchLlmInteractor(repo: LlmRepository): FetchLlmInteractor = FetchLlmInteractor { id ->
        repo.getLlm(id)
    }

    @Provides
    fun providesFetchLlmsInteractor(repo: LlmRepository): FetchLlmsInteractor = FetchLlmsInteractor {
            page,
            perPage,
            query,
            includeDefaults,
        ->
        repo.getLlms(page, perPage, query, includeDefaults)
    }

    @Provides
    fun providesSetPersonaNameInteractor(repo: PersonaRepository): SetPersonaNameInteractor =
        SetPersonaNameInteractor { name ->
            repo.withName(name)
        }

    @Provides
    fun providesSetPersonaAvatarInteractor(repo: PersonaRepository): SetPersonaAvatarInteractor =
        SetPersonaAvatarInteractor { avatarId, name ->
            repo.withAvatar(avatarId, name)
        }

    @Provides
    fun providesSetPersonaVoiceInteractor(repo: PersonaRepository): SetPersonaVoiceInteractor =
        SetPersonaVoiceInteractor { voiceId ->
            repo.withVoice(voiceId)
        }

    @Provides
    fun providesSetPersonaLlmInteractor(repo: PersonaRepository): SetPersonaLlmInteractor =
        SetPersonaLlmInteractor { llmId ->
            repo.withLlm(llmId)
        }

    @Provides
    fun providesSetPersonaSystemPromptInteractor(repo: PersonaRepository): SetPersonaSystemPromptInteractor =
        SetPersonaSystemPromptInteractor { systemPrompt ->
            repo.withSystemPrompt(systemPrompt)
        }

    @Provides
    fun providesFetchLicensesInteractor(licenseStore: LicenseStore): FetchLicensesInteractor = FetchLicensesInteractor {
        licenseStore.getLicenses()
    }
}
