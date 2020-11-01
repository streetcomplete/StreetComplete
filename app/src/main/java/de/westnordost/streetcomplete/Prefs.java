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
			SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS = "display.nonQuestionNotes",
			AUTOSYNC = "autosync",
			KEEP_SCREEN_ON = "display.keepScreenOn",
			UNGLUE_HINT_TIMES_SHOWN = "unglueHint.shown",
			THEME_SELECT = "theme.select",
			RESURVEY_INTERVALS = "quests.resurveyIntervals";


	public static final String
		OSM_USER_ID = "osm.userid",
		OSM_USER_NAME = "osm.username",
		OSM_UNREAD_MESSAGES = "osm.unread_messages",
		USER_DAYS_ACTIVE = "days_active",
		USER_GLOBAL_RANK = "user_global_rank",
		USER_LAST_TIMESTAMP_ACTIVE = "last_timestamp_active",
		IS_SYNCHRONIZING_STATISTICS = "is_synchronizing_statistics";

	// not shown anywhere directly
	public static final String
			QUEST_ORDER = "quests.order",
			QUEST_INVALIDATION = "quests.invalidation",
			LAST_SOLVED_QUEST_TIME = "changesets.lastQuestSolvedTime",
			MAP_LATITUDE = "map.latitude",
			MAP_LONGITUDE = "map.longitude",
			LAST_PICKED_PREFIX = "imageListLastPicked.",
			LAST_LOCATION_REQUEST_DENIED = "location.denied",
			LAST_VERSION = "lastVersion",
			LAST_VERSION_DATA = "lastVersion_data",
			HAS_SHOWN_TUTORIAL = "hasShownTutorial";

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

		public final int appCompatNightMode;
		Theme(int appCompatNightMode) { this.appCompatNightMode = appCompatNightMode; }
	}

	public enum ResurveyIntervals
	{
		LESS_OFTEN, DEFAULT, MORE_OFTEN
	}
}
