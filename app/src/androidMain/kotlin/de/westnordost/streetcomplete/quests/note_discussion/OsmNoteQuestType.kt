package de.westnordost.streetcomplete.quests.note_discussion

object OsmNoteQuestType : QuestType, AndroidQuest {
    override val icon = R.drawable.ic_quest_notes
    override val title = R.string.quest_noteDiscussion_title
    override val wikiLink = "Notes"
    override val achievements = emptyList<EditTypeAchievement>()
    override val visibilityEditable = false

    override fun createForm() = NoteDiscussionForm()
}
