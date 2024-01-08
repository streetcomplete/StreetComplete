package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.prefs.Preferences

class SelectedQuestPresetStore(private val prefs: Preferences) {

    fun get(): Long = prefs.getLong(Prefs.SELECTED_QUESTS_PRESET, 0)

    fun set(value: Long) {
        prefs.putLong(Prefs.SELECTED_QUESTS_PRESET, value)
    }
}
