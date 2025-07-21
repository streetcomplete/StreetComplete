package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.atp.atpquests.AtpQuestHidden
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.COMMENT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtp
import de.westnordost.streetcomplete.quests.getTitle
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.undo_delete
import de.westnordost.streetcomplete.resources.undo_move_node
import de.westnordost.streetcomplete.resources.undo_split
import de.westnordost.streetcomplete.resources.undo_visibility
import org.jetbrains.compose.resources.DrawableResource

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
    is AtpQuestHidden -> CreatePoiBasedOnAtp.icon
    else -> 0
}

val Edit.overlayIcon: DrawableResource? get() = when (this) {
    is ElementEdit -> {
        when (action) {
            is DeletePoiNodeAction -> Res.drawable.undo_delete
            is SplitWayAction -> Res.drawable.undo_split
            is MoveNodeAction -> Res.drawable.undo_move_node
            else -> null
        }
    }
    is OsmNoteQuestHidden -> Res.drawable.undo_visibility
    is OsmQuestHidden -> Res.drawable.undo_visibility
    is AtpQuestHidden -> Res.drawable.undo_visibility
    else -> null
}

@Composable
@ReadOnlyComposable
fun Edit.getTitle(elementTags: Map<String, String>?): String = when (this) {
    is ElementEdit -> {
        if (type is QuestType) {
            stringResource(type.getTitle(elementTags.orEmpty()))
        } else {
            stringResource(type.title)
        }
    }
    is NoteEdit -> {
        stringResource(when (action) {
            CREATE -> R.string.created_note_action_title
            COMMENT -> R.string.commented_note_action_title
        })
    }
    is OsmQuestHidden -> {
        stringResource(questType.getTitle(elementTags.orEmpty()))
    }
    is OsmNoteQuestHidden -> {
        stringResource(R.string.quest_noteDiscussion_title)
    }
    is AtpQuestHidden -> {
        stringResource(R.string.quest_atp_add_missing_poi_title)
    }
    else -> throw IllegalArgumentException()
}
