package ai.anam.lab.client.core.settings.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import platform.Foundation.NSUserDefaults

@ContributesTo(AppScope::class)
actual interface SettingsSubgraph {

    @Provides
    fun provideSettings(delegate: NSUserDefaults): ObservableSettings = NSUserDefaultsSettings(delegate)

    @Provides
    fun provideNsUserDefaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults
}
