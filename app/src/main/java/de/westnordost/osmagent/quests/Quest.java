package de.westnordost.osmagent.quests;

import de.westnordost.osmagent.quests.types.QuestType;

/** Represents one task for the user to complete/correct */
public interface Quest
{
	QuestType getType();

	QuestStatus getStatus();

	void setStatus(QuestStatus status);
}
