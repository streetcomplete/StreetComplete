package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import com.russhwolf.settings.ObservableSettings

class LastEditTimeStore(private val prefs: ObservableSettings) {

    fun touch() {
        prefs.putLong(Prefs.LAST_EDIT_TIME, nowAsEpochMilliseconds())
    }

    fun get(): Long =
        prefs.getLong(Prefs.LAST_EDIT_TIME, 0)
}
