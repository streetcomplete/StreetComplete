package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.COMMENT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden

val Edit.icon: Int get() = when (this) {
    is ElementEdit -> type.icon
    is NoteEdit -> {
        when (action) {
            CREATE -> R.drawable.ic_quest_create_note
            COMMENT -> R.drawable.ic_quest_notes
        }
    }
    is OsmNoteQuestHidden -> R.drawable.ic_quest_notes
    is OsmQuestHidden -> questType.icon
    else -> 0
}

val Edit.overlayIcon: Int get() = when (this) {
    is ElementEdit -> {
        when (action) {
            is DeletePoiNodeAction -> R.drawable.ic_undo_delete
            is SplitWayAction -> R.drawable.ic_undo_split
            is MoveNodeAction -> R.drawable.ic_undo_move_node
            else -> 0
        }
    }
    is OsmNoteQuestHidden -> R.drawable.ic_undo_visibility
    is OsmQuestHidden -> R.drawable.ic_undo_visibility
    else -> 0
}
