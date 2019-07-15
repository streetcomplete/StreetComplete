package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm

class OsmNoteQuestType : QuestType<NoteAnswer> {
    override fun getTitleReplacements(tags: Map<String, String>, featureName: Lazy<String?>): Array<String?> {
        return arrayOf()
    }

    override val icon = R.drawable.ic_quest_notes
    override val title = R.string.quest_noteDiscussion_title

    override fun createForm() = NoteDiscussionForm()
}
