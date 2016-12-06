package de.westnordost.streetcomplete.data;

import java.util.Collection;

import de.westnordost.osmapi.map.data.Element;

public interface VisibleQuestListener
{
	void onQuestCreated(Quest quest, QuestGroup group, Element element);

	void onQuestsRemoved(Collection<Long> questIds, QuestGroup group);
	void onQuestsCreated(Collection<? extends Quest> quests, QuestGroup group);
}
