package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm

class OsmNoteQuestType : QuestType<NoteAnswer> {
    override fun getTitleReplacements(tags: Map<String, String>, typeName: Lazy<String?>): Array<String?> {
        return arrayOf()
    }

    override val icon = R.drawable.ic_quest_notes
    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_noteDiscussion_title

    override fun createForm() = NoteDiscussionForm()
}
