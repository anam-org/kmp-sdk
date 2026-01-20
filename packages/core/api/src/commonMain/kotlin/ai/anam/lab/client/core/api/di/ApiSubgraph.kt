package ai.anam.lab.client.core.api.di

import ai.anam.lab.client.core.api.AvatarApi
import ai.anam.lab.client.core.api.LlmApi
import ai.anam.lab.client.core.api.VoicesApi
import ai.anam.lab.client.core.api.createAvatarApi
import ai.anam.lab.client.core.api.createLlmApi
import ai.anam.lab.client.core.api.createVoicesApi
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface ApiSubgraph {
    @Provides
    @SingleIn(AppScope::class)
    fun providesAvatarApi(ktorfit: Ktorfit): AvatarApi = ktorfit.createAvatarApi()

    @Provides
    @SingleIn(AppScope::class)
    fun providesVoicesApi(ktorfit: Ktorfit): VoicesApi = ktorfit.createVoicesApi()

    @Provides
    @SingleIn(AppScope::class)
    fun providesLlmApi(ktorfit: Ktorfit): LlmApi = ktorfit.createLlmApi()
}
