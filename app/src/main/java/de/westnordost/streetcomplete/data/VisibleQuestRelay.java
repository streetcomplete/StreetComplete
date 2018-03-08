package de.westnordost.streetcomplete.data;


import java.util.Collection;
import java.util.Collections;

import de.westnordost.osmapi.map.data.Element;

/** Threadsafe relay for VisibleQuestListener
 *  (setting the listener and calling the listener methods can safely be done from different threads) */
public class VisibleQuestRelay implements VisibleQuestListener
{
	private VisibleQuestListener listener;

	public synchronized void setListener(VisibleQuestListener listener)
	{
		this.listener = listener;
	}

	@Override public void onQuestSelected(Quest quest, QuestGroup group, Element element)
	{
		if (listener != null) listener.onQuestSelected(quest, group, element);
	}

	@Override public void onQuestsRemoved(Collection<Long> questIds, QuestGroup group)
	{
		if (listener != null) listener.onQuestsRemoved(questIds, group);
	}

	@Override public void onQuestsCreated(Collection<? extends Quest> quests, QuestGroup group)
	{
		if (listener != null) listener.onQuestsCreated(quests, group);
	}

	@Override public void onQuestSolved(long questId, QuestGroup group)
	{
		if (listener != null) listener.onQuestSolved(questId, group);
	}

	public void onQuestRemoved(long questId, QuestGroup group)
	{
		onQuestsRemoved(Collections.singletonList(questId), group);
	}

	@Override public void onQuestReverted(long revertQuestId, QuestGroup group)
	{
		if (listener != null) listener.onQuestReverted(revertQuestId, group);
	}
}
