package de.westnordost.streetcomplete.data.visiblequests

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs

class SelectedQuestPresetStore(private val prefs: ObservableSettings) {

    fun get(): Long = prefs.getLong(Prefs.SELECTED_QUESTS_PRESET, 0)

    fun set(value: Long) {
        prefs.putLong(Prefs.SELECTED_QUESTS_PRESET, value)
    }
}
