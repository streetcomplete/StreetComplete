package de.westnordost.streetcomplete

import androidx.appcompat.app.AppCompatDelegate

/** Constant class to have all the identifiers for shared preferences in one place  */
object Prefs {
    const val OAUTH = "oauth"
    const val OAUTH_ACCESS_TOKEN = "oauth.accessToken"
    const val OAUTH_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret"
    const val MAP_TILECACHE_IN_MB = "map.tilecache"
    const val SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS = "display.nonQuestionNotes"
    const val AUTOSYNC = "autosync"
    const val KEEP_SCREEN_ON = "display.keepScreenOn"
    const val UNGLUE_HINT_TIMES_SHOWN = "unglueHint.shown"
    const val THEME_SELECT = "theme.select"
    const val LANGUAGE_SELECT = "language.select"
    const val THEME_BACKGROUND = "theme.background_type"

    const val RESURVEY_INTERVALS = "quests.resurveyIntervals"

    const val OSM_USER_ID = "osm.userid"
    const val OSM_USER_NAME = "osm.username"
    const val OSM_UNREAD_MESSAGES = "osm.unread_messages"
    const val OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP = "osm.logged_in_after_oauth_fuckup"
    const val USER_DAYS_ACTIVE = "days_active"
    const val USER_GLOBAL_RANK = "user_global_rank"
    const val USER_GLOBAL_RANK_CURRENT_WEEK = "user_global_rank_current_week"
    const val USER_LAST_TIMESTAMP_ACTIVE = "last_timestamp_active"
    const val ACTIVE_DATES_RANGE = "active_days_range"
    const val IS_SYNCHRONIZING_STATISTICS = "is_synchronizing_statistics"
    const val TEAM_MODE_INDEX_IN_TEAM = "team_mode.index_in_team"
    const val TEAM_MODE_TEAM_SIZE = "team_mode.team_size"

    // not shown anywhere directly
    const val SELECTED_QUESTS_PRESET = "selectedQuestsPreset"
    const val LAST_EDIT_TIME = "changesets.lastChangeTime"
    const val MAP_LATITUDE = "map.latitude"
    const val MAP_LONGITUDE = "map.longitude"
    const val LAST_PICKED_PREFIX = "imageListLastPicked."
    const val LAST_VERSION = "lastVersion"
    const val LAST_VERSION_DATA = "lastVersion_data"
    const val HAS_SHOWN_TUTORIAL = "hasShownTutorial"
    const val QUEST_SELECTION_HINT_STATE = "questSelectionHintState"
    const val SELECTED_OVERLAY = "selectedOverlay"

    const val PIN_SPRITES_VERSION = "TangramPinsSpriteSheet.version"
    const val PIN_SPRITES = "TangramPinsSpriteSheet.sprites"

    const val ICON_SPRITES_VERSION = "TangramIconsSpriteSheet.version"
    const val ICON_SPRITES = "TangramIconsSpriteSheet.sprites"

    const val PREFERRED_LANGUAGE_FOR_NAMES = "preferredLanguageForNames"

    const val LAST_SHOWN_USER_GLOBAL_RANK = "last_shown.user_global_rank"
    const val LAST_SHOWN_USER_LOCAL_RANK = "last_shown.user_local_rank"
    const val LAST_SHOWN_USER_GLOBAL_RANK_CURRENT_WEEK = "last_shown.user_global_rank_current_week"
    const val LAST_SHOWN_USER_LOCAL_RANK_CURRENT_WEEK = "last_shown.user_local_rank_current_week"

    // modified
    const val VOLUME_ZOOM = "volume_button_zoom"
    const val SHOW_3D_BUILDINGS = "3d_buildings"
    const val QUEST_GEOMETRIES = "quest_geometries"
    const val AUTO_DOWNLOAD = "auto_download"
    const val GPS_INTERVAL = "gps_interval"
    const val NETWORK_INTERVAL = "network_interval"
    const val HIDE_NOTES_BY_USERS = "hide_notes_by_users"
    const val MANUAL_DOWNLOAD_OVERRIDE_CACHE = "manual_download_override_cache"
    const val QUICK_SETTINGS = "quick_settings"
    const val ALLOWED_LEVEL = "allowed_level"
    const val ALLOWED_LEVEL_TAGS = "allowed_level_tags"
    const val RESURVEY_KEYS = "resurvey_keys"
    const val RESURVEY_DATE = "resurvey_date"
    const val GPX_BUTTON = "gpx_button"
    const val SWAP_GPX_NOTE_BUTTONS = "swap_gpx_note_buttons"
    const val HIDE_KEYBOARD_FOR_NOTE = "hide_keyboard_for_note"
    const val OFFSET_FIX = "offset_fix"
    const val DAY_NIGHT_BEHAVIOR = "day_night_behavior"
    const val QUEST_SETTINGS_PER_PRESET = "quest_settings_per_preset"
    const val SHOW_HIDE_BUTTON = "show_hide_button"
    const val SELECT_FIRST_EDIT = "select_first_edit"
    const val BAN_CHECK_ERROR_COUNT = "ban_check_error_count"
    const val DATA_RETAIN_TIME = "data_retain_time"
    const val FAVS_FIRST_MIN_LINES = "favs_first_min_lines"
    const val SHOW_NEARBY_QUESTS = "show_nearby_quests"
    const val SHOW_NEARBY_QUESTS_DISTANCE = "show_nearby_quests_distance"
    const val CLOSE_FORM_IMMEDIATELY_AFTER_SOLVING = "close_form_immediately"
    const val CUSTOM_OVERLAY_FILTER = "custom_overlay_filter"
    const val CUSTOM_OVERLAY_COLOR_KEY = "custom_overlay_color_key"
    const val SHOW_SOLVED_ANIMATION = "show_solved_animation"
    const val PREFER_EXTERNAL_SD = "prefer_external_sd"
    const val SHOW_NEXT_QUEST_IMMEDIATELY = "show_next_quest_immediately"
    const val MAIN_MENU_FULL_GRID = "main_menu_full_grid"
    const val CREATE_POI_RECENT_FEATURE_IDS = "create_poi_recent_feature_ids"
    const val DYNAMIC_QUEST_CREATION = "dynamic_quest_creation"
    const val QUEST_MONITOR = "quest_monitor"
    const val SHOW_GPX_TRACK = "show_gpx_track"
    const val RASTER_TILE_URL = "raster_tile_url"
    const val CREATE_EXTERNAL_QUESTS = "create_external_quests"
    const val SAVE_PHOTOS = "save_photos"

    enum class Autosync {
        ON, WIFI, OFF
    }

    enum class Theme(val appCompatNightMode: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        DARK_CONTRAST(AppCompatDelegate.MODE_NIGHT_YES),
        AUTO(AppCompatDelegate.MODE_NIGHT_AUTO),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    enum class ResurveyIntervals {
        EVEN_LESS_OFTEN, LESS_OFTEN, DEFAULT, MORE_OFTEN
    }

    enum class DayNightBehavior {
        IGNORE,
        PRIORITY,
        VISIBILITY
    }
}
