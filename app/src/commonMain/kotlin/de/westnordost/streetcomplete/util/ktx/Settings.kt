package de.westnordost.streetcomplete.util.ktx

import com.russhwolf.settings.Settings

/** Stores the `String` [value] at [key] or removes the [key] if [value] is `null` */
fun Settings.putStringOrNull(key: String, value: String?) {
    if (value != null) putString(key, value) else remove(key)
}
