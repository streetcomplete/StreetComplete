package de.westnordost.streetcomplete.util.prefs

interface Preferences {
    val keys: Set<String>

    fun putBoolean(key: String, boolean: Boolean)
    fun putInt(key: String, int: Int)
    fun putLong(key: String, long: Long)
    fun putFloat(key: String, float: Float)
    fun putDouble(key: String, double: Double)
    fun putString(key: String, string: String?)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getInt(key: String, defaultValue: Int): Int
    fun getLong(key: String, defaultValue: Long): Long
    fun getFloat(key: String, defaultValue: Float): Float
    fun getDouble(key: String, defaultValue: Double): Double
    fun getStringOrNull(key: String): String?

    fun remove(key: String)

    fun addListener(key: String, callback: () -> Unit)
    fun removeListener(key: String, callback: () -> Unit)
}
