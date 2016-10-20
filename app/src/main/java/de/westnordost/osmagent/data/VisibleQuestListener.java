package de.westnordost.osmagent.data;

public interface VisibleQuestListener
{
	void onQuestCreated(Quest quest, QuestGroup group);
	void onQuestRemoved(Quest quest, QuestGroup group);
}
