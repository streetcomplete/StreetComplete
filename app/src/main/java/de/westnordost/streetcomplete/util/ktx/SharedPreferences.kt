package de.westnordost.streetcomplete.util.ktx

import android.content.SharedPreferences

fun SharedPreferences.containsAll(keys: List<String>): Boolean = keys.all { contains(it) }

fun SharedPreferences.Editor.putDouble(key: String, value: Double): SharedPreferences.Editor {
    return putLong(key, value.toRawBits())
}

fun SharedPreferences.getDouble(key: String, defValue: Double = 0.0): Double {
    return Double.fromBits(getLong(key, defValue.toRawBits()))
}
