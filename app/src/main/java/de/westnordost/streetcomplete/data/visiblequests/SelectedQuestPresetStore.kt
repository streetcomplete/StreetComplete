package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs

class SelectedQuestPresetStore(private val prefs: SharedPreferences) {

    fun get(): Long = prefs.getLong(Prefs.SELECTED_QUESTS_PRESET, 0)

    fun set(value: Long) {
        prefs.edit { putLong(Prefs.SELECTED_QUESTS_PRESET, value) }
    }
}
