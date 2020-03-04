package de.westnordost.streetcomplete;

import androidx.appcompat.app.AppCompatDelegate;

/** Constant class to have all the identifiers for shared preferences in one place */
public class Prefs
{
	public static final String
			OAUTH = "oauth",
			OAUTH_ACCESS_TOKEN = "oauth.accessToken",
			OAUTH_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret",
			MAP_TILECACHE_IN_MB = "map.tilecache",
			OSM_USER_ID = "osm.userid",
			OSM_USER_NAME = "osm.username",
			SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS = "display.nonQuestionNotes",
			AUTOSYNC = "autosync",
			KEEP_SCREEN_ON = "display.keepScreenOn",
			UNGLUE_HINT_TIMES_SHOWN = "unglueHint.shown",
			THEME_SELECT = "theme.select",
			OVERPASS_URL = "overpass_url";


	// not shown anywhere directly
	public static final String
			QUEST_ORDER = "quests.order",
			QUEST_INVALIDATION = "quests.invalidation",
			LAST_SOLVED_QUEST_TIME = "changesets.lastQuestSolvedTime",
			MAP_LATITUDE = "map.latitude",
			MAP_LONGITUDE = "map.longitude",
			LAST_PICKED_PREFIX = "imageListLastPicked.",
			LAST_LOCATION_REQUEST_DENIED = "location.denied",
			LAST_VERSION = "lastVersion";

	public static final String HAS_SHOWN_UNDO_FUCKUP_WARNING = "alert.undo_fuckup_warning";

	public static final String QUEST_SPRITES_VERSION = "TangramQuestSpriteSheet.version";
	public static final String QUEST_SPRITES = "TangramQuestSpriteSheet.questSprites";

	public enum Autosync
	{
		ON, WIFI, OFF
	}

	public enum Theme
	{
		LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
		DARK(AppCompatDelegate.MODE_NIGHT_YES),
		AUTO(AppCompatDelegate.MODE_NIGHT_AUTO),
		SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

		public int appCompatNightMode;
		Theme(int appCompatNightMode) { this.appCompatNightMode = appCompatNightMode; }
	}
}
