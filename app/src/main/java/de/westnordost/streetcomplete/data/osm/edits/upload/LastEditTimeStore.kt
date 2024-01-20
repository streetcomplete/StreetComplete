package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.prefs.Preferences

class LastEditTimeStore(private val prefs: Preferences) {

    fun touch() {
        prefs.putLong(Prefs.LAST_EDIT_TIME, nowAsEpochMilliseconds())
    }

    fun get(): Long =
        prefs.getLong(Prefs.LAST_EDIT_TIME, 0)
}
