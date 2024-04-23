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

    enum class Autosync {
        ON,
        WIFI,
        OFF
    }

    enum class Theme(val appCompatNightMode: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        AUTO(AppCompatDelegate.MODE_NIGHT_AUTO),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    enum class ResurveyIntervals {
        LESS_OFTEN,
        DEFAULT,
        MORE_OFTEN
    }
}
