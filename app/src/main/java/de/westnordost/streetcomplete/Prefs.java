package de.westnordost.streetcomplete;

/** Constant class to have all the identifiers for shared preferences in one place */
public class Prefs
{
	public static final String
			OAUTH = "oauth",
			OAUTH_ACCESS_TOKEN = "oauth.accessToken",
			OAUTH_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret",
			MAP_TILECACHE = "map.tilecache",
			OSM_USER_ID = "osm.userid",
			OSM_USER_NAME = "osm.username",
			SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS = "display.nonQuestionNotes",
			AUTOSYNC = "autosync",
			LAST_SOLVED_QUEST_TIME = "changesets.lastQuestSolvedTime",
			KEEP_SCREEN_ON = "display.keepScreenOn",
			UNGLUE_HINT_TIMES_SHOWN = "unglueHint.shown";

	// not shown anywhere directly
	public static final String
			QUEST_ORDER = "quests.order",
			QUEST_INVALIDATION = "quests.invalidation";

	public enum Autosync
	{
		ON, WIFI, OFF
	}
}
