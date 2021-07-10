package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import javax.inject.Inject

class SelectedQuestProfileStore @Inject constructor(private val prefs: SharedPreferences) {
    fun get(): Int = prefs.getInt(Prefs.SELECTED_QUESTS_PROFILE, 0)

    fun set(value: Int) {
        prefs.edit().putInt(Prefs.SELECTED_QUESTS_PROFILE, value).apply()
    }
}
