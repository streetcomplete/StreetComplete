package de.westnordost.streetcomplete.util.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.util.Listeners

class AndroidPreferences(private val prefs: SharedPreferences) : Preferences {

    private val listeners = Listeners<Pair<String, () -> Unit>>()

    private val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            listeners.forEach { (k, callback) ->
                if (k == key) callback()
            }
        }

    init {
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    override val keys: Set<String>
        get() = prefs.all.keys

    override fun putBoolean(key: String, boolean: Boolean) {
        prefs.edit { putBoolean(key, boolean) }
    }

    override fun putInt(key: String, int: Int) {
        prefs.edit { putInt(key, int) }
    }

    override fun putLong(key: String, long: Long) {
        prefs.edit { putLong(key, long) }
    }

    override fun putFloat(key: String, float: Float) {
        prefs.edit { putFloat(key, float) }
    }

    override fun putDouble(key: String, double: Double) {
        prefs.edit { putLong(key, double.toRawBits()) }
    }

    override fun putString(key: String, string: String?) {
        prefs.edit { putString(key, string) }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return prefs.getFloat(key, defaultValue)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return Double.fromBits(prefs.getLong(key, defaultValue.toRawBits()))
    }

    override fun getStringOrNull(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    override fun addListener(key: String, callback: () -> Unit) {
        listeners.add(key to callback)
    }
    override fun removeListener(key: String, callback: () -> Unit) {
        listeners.remove(key to callback)
    }
}
