package de.westnordost.streetcomplete.data;


import java.util.Collection;
import java.util.Collections;

/** Threadsafe relay for VisibleQuestListener
 *  (setting the listener and calling the listener methods can safely be done from different threads) */
public class VisibleQuestRelay implements VisibleQuestListener
{
	private VisibleQuestListener listener;

	public synchronized void setListener(VisibleQuestListener listener)
	{
		this.listener = listener;
	}

	@Override public void onQuestsRemoved(Collection<Long> questIds, QuestGroup group)
	{
		if (listener != null) listener.onQuestsRemoved(questIds, group);
	}

	@Override public void onQuestsCreated(Collection<? extends Quest> quests, QuestGroup group)
	{
		if (listener != null) listener.onQuestsCreated(quests, group);
	}

	public void onQuestRemoved(long questId, QuestGroup group)
	{
		onQuestsRemoved(Collections.singletonList(questId), group);
	}
}
