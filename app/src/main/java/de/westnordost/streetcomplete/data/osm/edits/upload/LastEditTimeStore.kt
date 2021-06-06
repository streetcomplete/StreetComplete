package de.westnordost.streetcomplete.data.osm.edits.upload

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import java.lang.System.currentTimeMillis
import javax.inject.Inject

class LastEditTimeStore @Inject constructor(private val prefs: SharedPreferences) {

    fun touch() {
        prefs.edit().putLong(Prefs.LAST_EDIT_TIME, currentTimeMillis()).apply()
    }

    fun get(): Long =
        prefs.getLong(Prefs.LAST_EDIT_TIME, 0)
}
