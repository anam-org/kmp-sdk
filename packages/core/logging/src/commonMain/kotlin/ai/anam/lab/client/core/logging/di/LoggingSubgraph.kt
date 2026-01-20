package ai.anam.lab.client.core.logging.di

import ai.anam.lab.client.core.logging.KermitLogger
import ai.anam.lab.client.core.logging.Logger
import co.touchlab.kermit.Logger as Kermit
import co.touchlab.kermit.Severity as KermitSeverity
import co.touchlab.kermit.StaticConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface LoggingSubgraph {
    @Provides
    @SingleIn(AppScope::class)
    fun providesLogger(): Logger = KermitLogger(
        Kermit(
            config = StaticConfig(
                minSeverity = KermitSeverity.Debug,
            ),
        ),
    )
}
