package ai.anam.lab.client.core.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.StorageSettings

/**
 * Wraps [StorageSettings] to implement [ObservableSettings] for wasmJs.
 * Listener methods are no-op (callbacks are not invoked on storage changes from other tabs);
 * [toFlowSettings] will emit the initial value and not subsequent external changes.
 */
internal class LocalStorageObservableSettings(delegate: StorageSettings) :
    ObservableSettings,
    Settings by delegate {

    override fun addIntListener(key: String, defaultValue: Int, callback: (Int) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addIntOrNullListener(key: String, callback: (Int?) -> Unit): SettingsListener = NoOpSettingsListener

    override fun addLongListener(key: String, defaultValue: Long, callback: (Long) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addLongOrNullListener(key: String, callback: (Long?) -> Unit): SettingsListener = NoOpSettingsListener

    override fun addStringListener(key: String, defaultValue: String, callback: (String) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addStringOrNullListener(key: String, callback: (String?) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addBooleanListener(key: String, defaultValue: Boolean, callback: (Boolean) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addBooleanOrNullListener(key: String, callback: (Boolean?) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addFloatListener(key: String, defaultValue: Float, callback: (Float) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addFloatOrNullListener(key: String, callback: (Float?) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addDoubleListener(key: String, defaultValue: Double, callback: (Double) -> Unit): SettingsListener =
        NoOpSettingsListener

    override fun addDoubleOrNullListener(key: String, callback: (Double?) -> Unit): SettingsListener =
        NoOpSettingsListener

    private object NoOpSettingsListener : SettingsListener {
        override fun deactivate() {}
    }
}
