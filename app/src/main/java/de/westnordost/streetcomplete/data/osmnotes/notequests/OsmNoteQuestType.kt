package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm

object OsmNoteQuestType : QuestType {
    override val icon = R.drawable.ic_quest_notes
    override val title = R.string.quest_noteDiscussion_title
    override val wikiLink = "Notes"
    override val achievements = emptyList<EditTypeAchievement>()

    override fun createForm() = NoteDiscussionForm()
}
