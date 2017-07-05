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
	INVISIBLE,
	/** the quest has been uploaded (either solved or dropped through conflict). The app needs to
	 * remember its solved quests for some time before deleting them because the source the app
	 * is pulling it's data for creating quests from (usually Overpass) lags behind the database
	 * where the app is uploading its changes to.
	 * Note quests are generally closed after upload, they are never deleted */
	CLOSED
}
