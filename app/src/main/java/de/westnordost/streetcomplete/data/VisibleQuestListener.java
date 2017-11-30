package de.westnordost.streetcomplete.data;

import java.util.Collection;

import de.westnordost.osmapi.map.data.Element;

public interface VisibleQuestListener
{
	void onQuestCreated(Quest quest, QuestGroup group, Element element);
	void onQuestsCreated(Collection<? extends Quest> quests, QuestGroup group);

	// after creation, two things can happen to quests. Either they are hidden or they are solved.

	void onQuestSolved(long questId, QuestGroup group);
	/** Called when the given quests are removed without being solved. I.e. hidden by the user or
	 *  when they become obsolete. */
	void onQuestsRemoved(Collection<Long> questIds, QuestGroup group);

	// after revert

	void onQuestReverted(long revertQuestId, QuestGroup group);
}
