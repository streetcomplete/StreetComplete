package de.westnordost.streetcomplete;

import android.content.SharedPreferences;

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
			KEEP_SCREEN_ON = "display.keepScreenOn",
			UNGLUE_HINT_TIMES_SHOWN = "unglueHint.shown",
			USER_HEIGHT = "user.height",
			HEIGHT_MEASURE_HINT_TIMES_SHOWN = "measureHint.shown",
			MEASURE_HINT_MARK_TOP_TIMES_SHOWN = "measureHint.markTop.shown",
			MEASURE_HINT_MARK_BOTTOM_TIMES_SHOWN = "measureHint.markBottom.shown";

	// not shown anywhere directly
	public static final String
			QUEST_ORDER = "quests.order",
			QUEST_INVALIDATION = "quests.invalidation",
			LAST_SOLVED_QUEST_TIME = "changesets.lastQuestSolvedTime",
			MAP_LATITUDE = "map.latitude",
			MAP_LONGITUDE = "map.longitude";

	public static final String HAS_SHOWN_UNDO_FUCKUP_WARNING = "alert.undo_fuckup_warning";

	public enum Autosync
	{
		ON, WIFI, OFF
	}
}
