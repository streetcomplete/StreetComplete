package de.westnordost.streetcomplete

import androidx.appcompat.app.AppCompatDelegate

/** Constant class to have all the identifiers for shared preferences in one place  */
object Prefs {
    // application settings
    const val MAP_TILECACHE_IN_MB = "map.tilecache"
    const val SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS = "display.nonQuestionNotes"
    const val AUTOSYNC = "autosync"
    const val KEEP_SCREEN_ON = "display.keepScreenOn"
    const val THEME_SELECT = "theme.select"
    const val LANGUAGE_SELECT = "language.select"
    const val RESURVEY_INTERVALS = "quests.resurveyIntervals"

    // login and user
    const val OSM_USER_ID = "osm.userid"
    const val OSM_USER_NAME = "osm.username"
    const val OSM_UNREAD_MESSAGES = "osm.unread_messages"
    const val OAUTH2_ACCESS_TOKEN = "oauth2.accessToken"
    // old keys login keys
    const val OAUTH1_ACCESS_TOKEN = "oauth.accessToken"
    const val OAUTH1_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret"
    const val OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP = "osm.logged_in_after_oauth_fuckup"

    // team mode
    const val TEAM_MODE_INDEX_IN_TEAM = "team_mode.index_in_team"
    const val TEAM_MODE_TEAM_SIZE = "team_mode.team_size"

    // application version
    const val LAST_VERSION = "lastVersion"
    const val LAST_VERSION_DATA = "lastVersion_data"

    // main screen UI
    const val HAS_SHOWN_TUTORIAL = "hasShownTutorial"
    const val HAS_SHOWN_OVERLAYS_TUTORIAL = "hasShownOverlaysTutorial"
    const val QUEST_SELECTION_HINT_STATE = "questSelectionHintState"

    // map state
    const val MAP_LATITUDE = "map.latitude"
    const val MAP_LONGITUDE = "map.longitude"
    const val MAP_ROTATION = "map.rotation"
    const val MAP_TILT = "map.tilt"
    const val MAP_ZOOM = "map.zoom"
    const val MAP_FOLLOWING = "map.following"
    const val MAP_NAVIGATION_MODE = "map.navigation_mode"

    // tangram
    const val PIN_SPRITES_VERSION = "TangramPinsSpriteSheet.version"
    const val PIN_SPRITES = "TangramPinsSpriteSheet.sprites"

    const val ICON_SPRITES_VERSION = "TangramIconsSpriteSheet.version"
    const val ICON_SPRITES = "TangramIconsSpriteSheet.sprites"

    // quest & overlays
    const val SELECTED_QUESTS_PRESET = "selectedQuestsPreset"
    const val SELECTED_OVERLAY = "selectedOverlay"
    const val LAST_PICKED_PREFIX = "imageListLastPicked."
    const val PREFERRED_LANGUAGE_FOR_NAMES = "preferredLanguageForNames"

    const val LAST_EDIT_TIME = "changesets.lastChangeTime"

    // profile & statistics screen UI
    const val USER_DAYS_ACTIVE = "days_active"
    const val USER_GLOBAL_RANK = "user_global_rank"
    const val USER_GLOBAL_RANK_CURRENT_WEEK = "user_global_rank_current_week"
    const val USER_LAST_TIMESTAMP_ACTIVE = "last_timestamp_active"
    const val ACTIVE_DATES_RANGE = "active_days_range"
    const val IS_SYNCHRONIZING_STATISTICS = "is_synchronizing_statistics"

    // modified
    const val VOLUME_ZOOM = "volume_button_zoom"
    const val SHOW_3D_BUILDINGS = "3d_buildings"
    const val QUEST_GEOMETRIES = "quest_geometries"
    const val AUTO_DOWNLOAD = "auto_download"
    const val GPS_INTERVAL = "gps_interval"
    const val NETWORK_INTERVAL = "network_interval"
    const val HIDE_NOTES_BY_USERS = "hide_notes_by_users2"
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
    const val CUSTOM_OVERLAY_IDX_FILTER = "custom_overlay_idx_filter"
    const val CUSTOM_OVERLAY_IDX_COLOR_KEY = "custom_overlay_idx_color_key"
    const val CUSTOM_OVERLAY_IDX_NAME = "custom_overlay_idx_name"
    const val CUSTOM_OVERLAY_IDX_ICON = "custom_overlay_idx_icon"
    const val CUSTOM_OVERLAY_IDX_DASH_FILTER = "custom_overlay_idx_dash_filter"
    const val CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA = "custom_overlay_idx_highlight_missing_data"
    const val CUSTOM_OVERLAY_INDICES = "custom_overlay_indices"
    const val CUSTOM_OVERLAY_SELECTED_INDEX = "custom_overlay_selected_index"
    const val SHOW_SOLVED_ANIMATION = "show_solved_animation"
    const val PREFER_EXTERNAL_SD = "prefer_external_sd"
    const val SHOW_NEXT_QUEST_IMMEDIATELY = "show_next_quest_immediately"
    const val MAIN_MENU_FULL_GRID = "main_menu_full_grid"
    const val CREATE_POI_RECENT_FEATURE_IDS = "create_poi_recent_feature_ids"
    const val DYNAMIC_QUEST_CREATION = "dynamic_quest_creation"
    const val QUEST_MONITOR = "quest_monitor"
    const val QUEST_MONITOR_GPS = "quest_monitor_gps"
    const val QUEST_MONITOR_NET = "quest_monitor_net"
    const val QUEST_MONITOR_RADIUS = "quest_monitor_radius"
    const val QUEST_MONITOR_DOWNLOAD = "quest_monitor_download"
    const val SHOW_GPX_TRACK = "show_gpx_track"
    const val RASTER_TILE_URL = "raster_tile_url"
    const val CREATE_EXTERNAL_QUESTS = "create_external_quests"
    const val SAVE_PHOTOS = "save_photos"
    const val EXPERT_MODE = "expert_mode"
    const val SHOW_WAY_DIRECTION = "show_way_direction"
    const val SEARCH_MORE_LANGUAGES = "search_more_languages"
    const val NO_SATELLITE_LABEL = "no_satellite_label"
    const val CAPS_WORD_NAME_INPUT = "caps_word_name_input"
    const val INSERT_NODE_RECENT_FEATURE_IDS = "insert_node_recent_feature_ids"
    const val OVERLAY_QUICK_SELECTOR = "overlay_quick_selector"
    const val CREATE_NODE_LAST_TAGS_FOR_FEATURE = "create_node_last_tags_for_"
    const val CREATE_NODE_SHOW_KEYBOARD = "create_node_show_keyboard"
    const val UPDATE_LOCAL_STATISTICS = "update_local_statistics"
    const val HIDE_OVERLAY_QUESTS = "hide_overlay_quests"
    const val MAIN_MENU_SWITCH_PRESETS = "main_menu_switch_presets"
    const val DISABLE_NAVIGATION_MODE = "disable_navigation_mode"
    const val TEMP_LOGGER = "temp_logger"
    const val THEME_BACKGROUND = "theme.background_type"

    enum class Autosync {
        ON,
        WIFI,
        OFF
    }

    enum class Theme(val appCompatNightMode: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        DARK_CONTRAST(AppCompatDelegate.MODE_NIGHT_YES),
        AUTO(AppCompatDelegate.MODE_NIGHT_AUTO),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    enum class ResurveyIntervals {
        EVEN_LESS_OFTEN,
        LESS_OFTEN,
        DEFAULT,
        MORE_OFTEN,
    }

    enum class DayNightBehavior {
        IGNORE,
        PRIORITY,
        VISIBILITY
    }
}
