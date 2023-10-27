package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.prefs.Preferences

class NotesPreferences(private val prefs: Preferences) {

    interface Listener {
        fun onNotesPreferencesChanged()
    }

    var listener: Listener? = null

    private val preferencesListener = object : Preferences.Listener {
        override fun onPreferencesChanged(key: String) {
            if (key == Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS) {
                listener?.onNotesPreferencesChanged()
            }
        }
    }

    init {
        prefs.addListener(preferencesListener)
    }

    val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        !prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)
}
