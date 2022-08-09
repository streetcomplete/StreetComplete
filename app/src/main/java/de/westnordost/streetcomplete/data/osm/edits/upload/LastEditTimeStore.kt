package de.westnordost.streetcomplete.data.osm.edits.upload

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import java.lang.System.currentTimeMillis

class LastEditTimeStore(private val prefs: SharedPreferences) {

    fun touch() {
        prefs.edit { putLong(Prefs.LAST_EDIT_TIME, currentTimeMillis()) }
    }

    fun get(): Long =
        prefs.getLong(Prefs.LAST_EDIT_TIME, 0)
}
