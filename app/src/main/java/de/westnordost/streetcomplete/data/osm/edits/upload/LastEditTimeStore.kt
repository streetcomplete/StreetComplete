package de.westnordost.streetcomplete.data.osm.edits.upload

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

class LastEditTimeStore(private val prefs: ObservableSettings) {

    fun touch() {
        prefs.putLong(Prefs.LAST_EDIT_TIME, nowAsEpochMilliseconds())
    }

    fun get(): Long =
        prefs.getLong(Prefs.LAST_EDIT_TIME, 0)
}
