package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class SelectedQuestProfileStore @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun get(): Long = prefs.getLong(Prefs.SELECTED_QUESTS_PROFILE, 0)

    fun set(value: Long) {
        prefs.edit().putLong(Prefs.SELECTED_QUESTS_PROFILE, value).apply()
    }
}
