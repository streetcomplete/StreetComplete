package de.westnordost.osmagent.quests;

public enum QuestStatus
{
	/** just created */
	NEW,
	/** user answered the question */
	ANSWERED,
	/** user chose to hide the quest */
	CANCELED,
	/** user could not answer the question and left a note instead */
	LEFT_NOTE
}
