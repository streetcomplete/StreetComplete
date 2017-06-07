package de.westnordost.streetcomplete.data;

public enum QuestStatus
{
	/** just created */
	NEW,
	/** user answered the question (waiting for changes to be uploaded) */
	ANSWERED,
	/** user chose to hide the quest */
	HIDDEN,
	/** the system (decided that it) doesn't show the quest. They may become visible again (-> NEW) */
	INVISIBLE
}
