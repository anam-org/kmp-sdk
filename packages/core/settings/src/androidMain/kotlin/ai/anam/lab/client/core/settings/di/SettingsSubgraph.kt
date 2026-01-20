package ai.anam.lab.client.core.settings.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
actual interface SettingsSubgraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideSettings(delegate: SharedPreferences): ObservableSettings {
        return SharedPreferencesSettings(delegate)
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesSharedPreferences(context: Application): SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences",
        Context.MODE_PRIVATE,
    )
}
