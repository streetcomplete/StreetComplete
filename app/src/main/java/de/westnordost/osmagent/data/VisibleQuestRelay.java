package de.westnordost.osmagent.data;


import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuest;
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

	@Override public synchronized void onQuestCreated(OsmQuest quest, Element element)
	{
		if (listener != null) listener.onQuestCreated(quest, element);
	}

	@Override public synchronized void onOsmQuestRemoved(long questId)
	{
		if (listener != null) listener.onOsmQuestRemoved(questId);
	}

	@Override public synchronized void onQuestCreated(OsmNoteQuest quest)
	{
		if (listener != null) listener.onQuestCreated(quest);
	}

	@Override public synchronized void onNoteQuestRemoved(long questId)
	{
		if (listener != null) listener.onNoteQuestRemoved(questId);
	}
}
