package ai.anam.lab.client.core.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.StorageSettings

/**
 * Wraps [StorageSettings] to implement [ObservableSettings] for wasmJs by manually
 * tracking listeners and invoking callbacks when settings are modified through this instance.
 *
 * Note: Listeners will only be invoked for changes made through this Settings instance.
 * Changes made directly to localStorage or from other tabs will not trigger callbacks.
 */
internal class LocalStorageObservableSettings(private val delegate: StorageSettings) :
    ObservableSettings,
    Settings by delegate {

    private val listeners = mutableMapOf<String, MutableSet<ListenerWrapper>>()

    // region Write operations - override to notify listeners

    override fun putInt(key: String, value: Int) {
        delegate.putInt(key, value)
        notifyListeners(key)
    }

    override fun putLong(key: String, value: Long) {
        delegate.putLong(key, value)
        notifyListeners(key)
    }

    override fun putString(key: String, value: String) {
        delegate.putString(key, value)
        notifyListeners(key)
    }

    override fun putFloat(key: String, value: Float) {
        delegate.putFloat(key, value)
        notifyListeners(key)
    }

    override fun putDouble(key: String, value: Double) {
        delegate.putDouble(key, value)
        notifyListeners(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        delegate.putBoolean(key, value)
        notifyListeners(key)
    }

    override fun remove(key: String) {
        delegate.remove(key)
        notifyListeners(key)
    }

    override fun clear() {
        val keys = delegate.keys.toSet()
        delegate.clear()
        keys.forEach { notifyListeners(it) }
    }

    // endregion

    // region Listener registration

    override fun addIntListener(key: String, defaultValue: Int, callback: (Int) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getInt(key, defaultValue)) }

    override fun addIntOrNullListener(key: String, callback: (Int?) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getIntOrNull(key)) }

    override fun addLongListener(key: String, defaultValue: Long, callback: (Long) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getLong(key, defaultValue)) }

    override fun addLongOrNullListener(key: String, callback: (Long?) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getLongOrNull(key)) }

    override fun addStringListener(key: String, defaultValue: String, callback: (String) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getString(key, defaultValue)) }

    override fun addStringOrNullListener(key: String, callback: (String?) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getStringOrNull(key)) }

    override fun addFloatListener(key: String, defaultValue: Float, callback: (Float) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getFloat(key, defaultValue)) }

    override fun addFloatOrNullListener(key: String, callback: (Float?) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getFloatOrNull(key)) }

    override fun addDoubleListener(key: String, defaultValue: Double, callback: (Double) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getDouble(key, defaultValue)) }

    override fun addDoubleOrNullListener(key: String, callback: (Double?) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getDoubleOrNull(key)) }

    override fun addBooleanListener(
        key: String,
        defaultValue: Boolean,
        callback: (Boolean) -> Unit,
    ): SettingsListener = addListener(key) { callback(delegate.getBoolean(key, defaultValue)) }

    override fun addBooleanOrNullListener(key: String, callback: (Boolean?) -> Unit): SettingsListener =
        addListener(key) { callback(delegate.getBooleanOrNull(key)) }

    // endregion

    private fun addListener(key: String, onNotify: () -> Unit): SettingsListener {
        val wrapper = ListenerWrapper(onNotify)
        listeners.getOrPut(key) { mutableSetOf() }.add(wrapper)
        return object : SettingsListener {
            override fun deactivate() {
                listeners[key]?.remove(wrapper)
                if (listeners[key]?.isEmpty() == true) {
                    listeners.remove(key)
                }
            }
        }
    }

    private fun notifyListeners(key: String) {
        listeners[key]?.forEach { it.notify() }
    }

    private class ListenerWrapper(private val onNotify: () -> Unit) {
        fun notify() {
            onNotify()
        }
    }
}
