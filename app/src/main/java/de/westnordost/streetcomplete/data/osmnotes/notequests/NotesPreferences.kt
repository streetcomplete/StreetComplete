package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs

class NotesPreferences(private val prefs: SharedPreferences) {

    interface Listener {
        fun onNotesPreferencesChanged()
    }

    var listener: Listener? = null

    val blockedIds = mutableListOf<Long>().apply {
        addAll(prefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet())?.mapNotNull { it.toLongOrNull() } ?: emptyList())
    }

    val blockedNames = mutableSetOf<String>().apply {
        addAll(prefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet()) ?: emptySet())
    }
    private val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS) {
            listener?.onNotesPreferencesChanged()
        }
        if (key == Prefs.HIDE_NOTES_BY_USERS) {
            blockedIds.clear()
            blockedIds.addAll(prefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet())?.mapNotNull { it.toLongOrNull() } ?: emptyList())
            blockedNames.clear()
            blockedNames.addAll(prefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet()) ?: emptySet())
            listener?.onNotesPreferencesChanged()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        !prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)
}
