package de.westnordost.streetcomplete.util.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.util.Listeners

class AndroidPreferences(private val prefs: SharedPreferences) :
    Preferences,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val listeners = Listeners<Preferences.Listener>()

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

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

    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }

    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    override fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    override fun addListener(listener: Preferences.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Preferences.Listener) {
        listeners.remove(listener)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        listeners.forEach { it.onPreferencesChanged(key) }
    }
}
