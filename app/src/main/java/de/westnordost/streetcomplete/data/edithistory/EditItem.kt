package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.*

val Edit.icon: Int get() = when(this) {
    is ElementEdit -> questType.icon
    is NoteEdit -> {
        when(action) {
            CREATE -> R.drawable.ic_quest_create_note
            COMMENT -> R.drawable.ic_quest_notes
        }
    }
    else -> 0
}
