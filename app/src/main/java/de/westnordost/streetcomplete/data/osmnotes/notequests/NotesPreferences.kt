package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.prefs.Preferences

class NotesPreferences(private val prefs: Preferences) {

    interface Listener {
        fun onNotesPreferencesChanged()
    }

    var listener: Listener? = null

    init {
        prefs.addListener(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS) {
            listener?.onNotesPreferencesChanged()
        }
    }

    val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        !prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)
}
