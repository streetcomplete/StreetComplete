package de.westnordost.streetcomplete.data.osm.edits.upload

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

class LastEditTimeStore(private val prefs: SharedPreferences) {

    fun touch() {
        prefs.edit { putLong(Prefs.LAST_EDIT_TIME, nowAsEpochMilliseconds()) }
    }

    fun get(): Long =
        prefs.getLong(Prefs.LAST_EDIT_TIME, 0)
}
