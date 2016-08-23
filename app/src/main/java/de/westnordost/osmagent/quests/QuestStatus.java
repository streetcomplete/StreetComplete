package de.westnordost.osmagent.quests;

public enum QuestStatus
{
	/** just created */
	NEW,
	/** user read the question */
	READ,
	/** user answered the question */
	ANSWERED,
	/** user's answer was uploaded successfully to the server */
	COMPLETED,
	/** user's answer could not be applied because there was a conflict */
	CONFLICT,
	/** user chose to hide the quest */
	CANCELED,
	/** user could not answer the question and left a note instead */
	LEFT_NOTE
}
