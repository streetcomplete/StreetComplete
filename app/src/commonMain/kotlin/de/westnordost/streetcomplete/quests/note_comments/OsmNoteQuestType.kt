package de.westnordost.streetcomplete.quests.note_comments

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_noteDiscussion_title
import de.westnordost.streetcomplete.resources.quest_notes

object OsmNoteQuestType : QuestType {
    override val icon = Res.drawable.quest_notes
    override val title = Res.string.quest_noteDiscussion_title
    override val wikiLink = "Notes"
    override val achievements = emptyList<EditTypeAchievement>()
    override val visibilityEditable = false
}
