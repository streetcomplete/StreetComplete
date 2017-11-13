package de.westnordost.streetcomplete.data.osmnotes;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm;

public class OsmNoteQuestType implements QuestType
{
	@Override public AbstractQuestAnswerFragment createForm() { return new NoteDiscussionForm(); }
	@Override public int getIcon() { return R.drawable.ic_quest_notes; }
	@Override public int getTitle() { return R.string.quest_noteDiscussion_title; }

	@Override public int getDefaultDisabledMessage() { return 0; }
}
