package ai.anam.lab.client.core.test

import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.settings.Preference
import ai.anam.lab.client.core.settings.Theme

class FakeAnamPreferences : AnamPreferences {
    override val theme: Preference<Theme> = FakePreference(Theme.SYSTEM)
    override val apiKey: Preference<String> = FakePreference("")
}
