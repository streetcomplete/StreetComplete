package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

class LastEditTimeStore(private val prefs: Preferences) {

    // TODO hmmm
    fun touch() {
        prefs.lastEditTime = nowAsEpochMilliseconds()
    }

    fun get(): Long = prefs.lastEditTime
}
