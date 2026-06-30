package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.*

object OsmNoteQuestType : QuestType {
    override val icon = Res.drawable.quest_notes
    override val title = Res.string.quest_noteDiscussion_title
    override val wikiLink = "Notes"
    override val achievements = emptyList<EditTypeAchievement>()
    override val visibilityEditable = false

    @Composable
    fun Form(on: (NoteQuestAction) -> Unit, note: Note) {
        AddNoteCommentForm(on, note)
    }
}
