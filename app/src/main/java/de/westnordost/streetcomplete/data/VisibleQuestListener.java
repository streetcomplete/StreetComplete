package de.westnordost.streetcomplete.data;

import java.util.Collection;

public interface VisibleQuestListener
{
	void onQuestsCreated(Collection<? extends Quest> quests, QuestGroup group);
	/** Called when the given quests are removed I.e. solved, hidden by the user or when they become obsolete. */
	void onQuestsRemoved(Collection<Long> questIds, QuestGroup group);
}
