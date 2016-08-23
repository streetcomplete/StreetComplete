package de.westnordost.osmagent.quests;


import java.util.HashMap;

/** Holds all the quests... */
public class QuestManager
{
	private int idCounter = 0;
	private HashMap<Integer, OsmQuest> quests;

	public synchronized void add(OsmQuest quest)
	{
		quests.put(idCounter, quest);
		idCounter++;
	}

	public synchronized OsmQuest get(int questId)
	{
		return quests.get(questId);
	}

	public synchronized void remove(int questId)
	{
		quests.remove(questId);
	}
}
