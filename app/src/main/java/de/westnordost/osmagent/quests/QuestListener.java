package de.westnordost.osmagent.quests;

public interface QuestListener
{
	void onQuestCreated(Quest quest);
	void onQuestRemoved(Quest quest);
}
