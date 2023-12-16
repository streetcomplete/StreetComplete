package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.util.prefs.Preferences

class NotesPreferences(private val prefs: Preferences) {

    interface Listener {
        fun onNotesPreferencesChanged()
    }

    private val androidPrefs = StreetCompleteApplication.preferences

    var listener: Listener? = null

    val blockedIds = mutableListOf<Long>().apply {
        addAll(androidPrefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet())?.mapNotNull { it.toLongOrNull() } ?: emptyList())
    }

    val blockedNames = mutableSetOf<String>().apply {
        addAll(androidPrefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet()) ?: emptySet())
    }

    private val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Prefs.HIDE_NOTES_BY_USERS) {
            blockedIds.clear()
            blockedIds.addAll(androidPrefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet())?.mapNotNull { it.toLongOrNull() } ?: emptyList())
            blockedNames.clear()
            blockedNames.addAll(androidPrefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet()) ?: emptySet())
            listener?.onNotesPreferencesChanged()
        }
    }

    init {
        prefs.addListener(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS) {
            listener?.onNotesPreferencesChanged()
        }
        androidPrefs.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        !prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)
}
