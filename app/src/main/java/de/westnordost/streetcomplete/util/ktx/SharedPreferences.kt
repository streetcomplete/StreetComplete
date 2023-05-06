package de.westnordost.streetcomplete.util.ktx

import android.content.SharedPreferences

fun SharedPreferences.containsAll(keys: List<String>): Boolean = keys.all { contains(it) }
