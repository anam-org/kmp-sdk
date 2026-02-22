package ai.anam.lab.client.core.http.di

import ai.anam.lab.client.core.auth.AuthRepository
import ai.anam.lab.client.core.http.InvalidateAuthTokensInteractor
import ai.anam.lab.client.core.http.buildApiHttpClient
import ai.anam.lab.client.core.http.invalidateAuthTokens
import ai.anam.lab.client.core.logging.Logger
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@ContributesTo(AppScope::class)
interface HttpSubgraph {
    @Provides
    @SingleIn(AppScope::class)
    fun providesHttpClient(authRepository: AuthRepository, logger: Logger): HttpClient =
        buildApiHttpClient(token = authRepository::getApiToken, logger = logger)

    @Provides
    @SingleIn(AppScope::class)
    fun providesKtorfit(httpClient: HttpClient): Ktorfit = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl("https://api.anam.ai/")
        .build()

    @Provides
    fun providesInvalidateAuthTokensInteractor(httpClient: HttpClient): InvalidateAuthTokensInteractor =
        InvalidateAuthTokensInteractor { httpClient.invalidateAuthTokens() }
}
