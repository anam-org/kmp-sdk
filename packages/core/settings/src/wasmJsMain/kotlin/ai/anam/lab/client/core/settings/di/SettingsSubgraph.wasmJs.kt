package ai.anam.lab.client.core.settings.di

import ai.anam.lab.client.core.settings.LocalStorageObservableSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.localStorage

@ContributesTo(AppScope::class)
actual interface SettingsSubgraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideSettings(): ObservableSettings = LocalStorageObservableSettings(StorageSettings(localStorage))
}
