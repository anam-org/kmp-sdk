package ai.anam.lab.client.core.client.di

import ai.anam.lab.AnamClient
import ai.anam.lab.AnamClientOptions
import ai.anam.lab.PlatformContext
import ai.anam.lab.client.core.client.ApiConfig
import ai.anam.lab.client.core.http.ApiHttpConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface ClientSubgraph {
    @Provides
    @SingleIn(AppScope::class)
    fun providesApiConfig(): ApiConfig = ApiConfig()

    @Provides
    @SingleIn(AppScope::class)
    fun providesApiHttpConfig(apiConfig: ApiConfig): ApiHttpConfig = ApiHttpConfig(
        baseUrl = apiConfig.environment.baseUrl,
        requestTimeoutMs = apiConfig.requestTimeoutMs,
        uploadTimeoutMs = apiConfig.uploadTimeoutMs,
    )

    @Provides
    @SingleIn(AppScope::class)
    fun providesAnamClient(context: PlatformContext, apiConfig: ApiConfig): AnamClient = AnamClient(
        options = AnamClientOptions(
            context = context,
            environment = apiConfig.environment,
        ),
    )
}

/**
 * The [AnamClientOptions] require a [PlatformContext], which will be obtained via this Subgraph.
 */
expect interface ClientPlatformContextSubgraph
