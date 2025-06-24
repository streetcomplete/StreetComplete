package de.westnordost.streetcomplete.quests.note_discussion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement

object OsmNoteQuestType : QuestType, AndroidQuest {
    override val icon = R.drawable.ic_quest_notes
    override val title = R.string.quest_noteDiscussion_title
    override val wikiLink = "Notes"
    override val achievements = emptyList<EditTypeAchievement>()
    override val visibilityEditable = false

    override fun createForm() = NoteDiscussionForm()
}
