package de.westnordost.streetcomplete.data.preferences

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.boolean
import com.russhwolf.settings.double
import com.russhwolf.settings.float
import com.russhwolf.settings.int
import com.russhwolf.settings.long
import com.russhwolf.settings.nullableString
import com.russhwolf.settings.string
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.putStringOrNull

class Preferences(private val prefs: ObservableSettings) {
    // application settings
    var language: String? by prefs.nullableString(LANGUAGE_SELECT)

    var theme: Theme
        set(value) { prefs.putString(THEME_SELECT, value.name) }
        get() {
            val value = prefs.getStringOrNull(THEME_SELECT)
            // AUTO setting was removed because as of June 2024, 95% of active installs from
            // google play use an Android where AUTO is deprecated
            return if (value == "AUTO" || value == null) Theme.SYSTEM else Theme.valueOf(value)
        }

    var autosync: Autosync
        set(value) { prefs.putString(AUTOSYNC, value.name) }
        get() = prefs.getStringOrNull(AUTOSYNC)?.let { Autosync.valueOf(it) } ?: DEFAULT_AUTOSYNC

    var keepScreenOn: Boolean by prefs.boolean(KEEP_SCREEN_ON, false)

    var resurveyIntervals: ResurveyIntervals
        set(value) { prefs.putString(RESURVEY_INTERVALS, value.name) }
        get() = prefs.getStringOrNull(RESURVEY_INTERVALS)?.let { ResurveyIntervals.valueOf(it) }
            ?: DEFAULT_RESURVEY_INTERVALS

    var showAllNotes: Boolean by prefs.boolean(SHOW_ALL_NOTES, false)

    fun onLanguageChanged(callback: (String?) -> Unit): SettingsListener =
        prefs.addStringOrNullListener(LANGUAGE_SELECT, callback)

    fun onThemeChanged(callback: (Theme) -> Unit): SettingsListener =
        prefs.addStringOrNullListener(THEME_SELECT) {
            callback(it?.let { Theme.valueOf(it) } ?: DEFAULT_THEME)
        }

    fun onAutosyncChanged(callback: (Autosync) -> Unit): SettingsListener =
        prefs.addStringOrNullListener(AUTOSYNC) {
            callback(it?.let { Autosync.valueOf(it) } ?: DEFAULT_AUTOSYNC)
        }

    fun onResurveyIntervalsChanged(callback: (ResurveyIntervals) -> Unit): SettingsListener =
        prefs.addStringOrNullListener(RESURVEY_INTERVALS) {
            callback(it?.let { ResurveyIntervals.valueOf(it) } ?: DEFAULT_RESURVEY_INTERVALS)
        }

    fun onAllShowNotesChanged(callback: (Boolean) -> Unit): SettingsListener =
        prefs.addBooleanListener(SHOW_ALL_NOTES, false, callback)

    fun onKeepScreenOnChanged(callback: (Boolean) -> Unit): SettingsListener =
        prefs.addBooleanListener(KEEP_SCREEN_ON, false, callback)

    // login and user
    var userId: Long by prefs.long(OSM_USER_ID, -1)
    var userName: String? by prefs.nullableString(OSM_USER_NAME)
    var userUnreadMessages: Int by prefs.int(OSM_UNREAD_MESSAGES, 0)

    var oAuth2AccessToken: String? by prefs.nullableString(OAUTH2_ACCESS_TOKEN)
    val hasOAuth1AccessToken: Boolean get() = prefs.hasKey(OAUTH1_ACCESS_TOKEN)

    fun clearUserData() {
        prefs.remove(OSM_USER_ID)
        prefs.remove(OSM_USER_NAME)
        prefs.remove(OSM_UNREAD_MESSAGES)
    }

    fun removeOAuth1Data() {
        prefs.remove(OAUTH1_ACCESS_TOKEN)
        prefs.remove(OAUTH1_ACCESS_TOKEN_SECRET)
        prefs.remove(OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP)
    }

    // map state
    var mapPosition: LatLon
        set(value) {
            prefs.putDouble(MAP_LATITUDE, value.latitude)
            prefs.putDouble(MAP_LONGITUDE, value.longitude)
        }
        get() = LatLon(
            latitude = prefs.getDouble(MAP_LATITUDE, 0.0),
            longitude = prefs.getDouble(MAP_LONGITUDE, 0.0)
        )
    var mapRotation: Double by prefs.double(MAP_ROTATION, 0.0)
    var mapTilt: Double by prefs.double(MAP_TILT, 0.0)
    var mapZoom: Double by prefs.double(MAP_ZOOM, 0.0)
    var mapIsFollowing: Boolean by prefs.boolean(MAP_FOLLOWING, true)
    var mapIsNavigationMode: Boolean by prefs.boolean(MAP_NAVIGATION_MODE, false)

    var clearedTangramCache: Boolean by prefs.boolean(CLEARED_TANGRAM_CACHE, false)

    // application version
    var lastChangelogVersion: String? by prefs.nullableString(LAST_VERSION)
    var lastDataVersion: String? by prefs.nullableString(LAST_VERSION_DATA)

    // team mode
    var teamModeSize: Int by prefs.int(TEAM_MODE_TEAM_SIZE, -1)
    var teamModeIndexInTeam: Int by prefs.int(TEAM_MODE_INDEX_IN_TEAM, -1)

    // main screen UI
    var hasShownTutorial: Boolean by prefs.boolean(HAS_SHOWN_TUTORIAL, false)
    var hasShownOverlaysTutorial: Boolean by prefs.boolean(HAS_SHOWN_OVERLAYS_TUTORIAL, false)
    var questSelectionHintState: QuestSelectionHintState
        set(value) { prefs.putString(QUEST_SELECTION_HINT_STATE, value.name) }
        get() = prefs.getStringOrNull(QUEST_SELECTION_HINT_STATE)?.let { QuestSelectionHintState.valueOf(it) }
            ?: QuestSelectionHintState.NOT_SHOWN

    fun onQuestSelectionHintStateChanged(callback: (QuestSelectionHintState) -> Unit): SettingsListener =
        prefs.addStringOrNullListener(QUEST_SELECTION_HINT_STATE) {
            callback(it?.let { QuestSelectionHintState.valueOf(it) } ?: QuestSelectionHintState.NOT_SHOWN)
        }

    // quest & overlay UI
    var preferredLanguageForNames: String? by prefs.nullableString(PREFERRED_LANGUAGE_FOR_NAMES)
    var selectedQuestPreset: Long by prefs.long(SELECTED_QUESTS_PRESET, 0L)
    var selectedOverlayName: String? by prefs.nullableString(SELECTED_OVERLAY)

    fun onSelectedOverlayNameChanged(callback: (String?) -> Unit): SettingsListener =
        prefs.addStringOrNullListener(SELECTED_OVERLAY, callback)

    fun onSelectedQuestPresetChanged(callback: (Long) -> Unit): SettingsListener =
        prefs.addLongListener(SELECTED_QUESTS_PRESET, 0L, callback)

    var lastEditTime: Long by prefs.long(LAST_EDIT_TIME, 0L)

    fun getLastPicked(key: String): List<String> =
        prefs.getStringOrNull(LAST_PICKED_PREFIX + key)?.split(',') ?: listOf()

    fun addLastPicked(key: String, value: String, maxValueCount: Int = 100) {
        addLastPicked(key, listOf(value), maxValueCount)
    }

    fun addLastPicked(key: String, values: List<String>, maxValueCount: Int = 100) {
        val lastValues = values + getLastPicked(key)
        setLastPicked(key, lastValues.take(maxValueCount))
    }

    fun setLastPicked(key: String, values: List<String>) {
        prefs.putString(LAST_PICKED_PREFIX + key, values.joinToString(","))
    }

    fun getLastPickedLeft(key: String): String? =
        prefs.getStringOrNull(getLastPickedSideKey(key, "left"))

    fun setLastPickedLeft(key: String, value: String?) {
        prefs.putStringOrNull(getLastPickedSideKey(key, "left"), value)
    }

    fun getLastPickedRight(key: String): String? =
        prefs.getStringOrNull(getLastPickedSideKey(key, "right"))

    fun setLastPickedRight(key: String, value: String?) {
        prefs.putStringOrNull(getLastPickedSideKey(key, "right"), value)
    }

    fun getLastPickedOneSide(key: String): String? =
        prefs.getStringOrNull(getLastPickedSideKey(key, "oneSide"))

    fun setLastPickedOneSide(key: String, value: String?) {
        prefs.putStringOrNull(getLastPickedSideKey(key, "oneSide"), value)
    }

    private fun getLastPickedSideKey(key: String, side: String): String =
        "$LAST_PICKED_PREFIX$key.$side"

    // profile & statistics screen UI
    var userGlobalRank: Int by prefs.int(USER_GLOBAL_RANK, -1)
    var userGlobalRankCurrentWeek: Int by prefs.int(USER_GLOBAL_RANK_CURRENT_WEEK, -1)
    var userLastTimestampActive: Long by prefs.long(USER_LAST_TIMESTAMP_ACTIVE, 0)
    var userDaysActive: Int by prefs.int(USER_DAYS_ACTIVE, 0)
    var userActiveDatesRange: Int by prefs.int(ACTIVE_DATES_RANGE, 100)

    // default true because if it is not set yet, the first thing that is done is to synchronize it
    var isSynchronizingStatistics: Boolean by prefs.boolean(IS_SYNCHRONIZING_STATISTICS, true)

    fun clearUserStatistics() {
        prefs.remove(USER_DAYS_ACTIVE)
        prefs.remove(ACTIVE_DATES_RANGE)
        prefs.remove(IS_SYNCHRONIZING_STATISTICS)
        prefs.remove(USER_GLOBAL_RANK)
        prefs.remove(USER_GLOBAL_RANK_CURRENT_WEEK)
        prefs.remove(USER_LAST_TIMESTAMP_ACTIVE)
    }

    companion object {
        private val DEFAULT_AUTOSYNC = Autosync.ON
        private val DEFAULT_RESURVEY_INTERVALS = ResurveyIntervals.DEFAULT
        private val DEFAULT_THEME = Theme.SYSTEM

        // application settings
        private const val SHOW_ALL_NOTES = "display.nonQuestionNotes"
        private const val AUTOSYNC = "autosync"
        private const val KEEP_SCREEN_ON = "display.keepScreenOn"
        private const val THEME_SELECT = "theme.select"
        private const val LANGUAGE_SELECT = "language.select"
        private const val RESURVEY_INTERVALS = "quests.resurveyIntervals"

        // login and user
        private const val OSM_USER_ID = "osm.userid"
        private const val OSM_USER_NAME = "osm.username"
        private const val OSM_UNREAD_MESSAGES = "osm.unread_messages"
        private const val OAUTH2_ACCESS_TOKEN = "oauth2.accessToken"

        // old keys login keys
        private const val OAUTH1_ACCESS_TOKEN = "oauth.accessToken"
        private const val OAUTH1_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret"
        private const val OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP = "osm.logged_in_after_oauth_fuckup"

        // team mode
        private const val TEAM_MODE_INDEX_IN_TEAM = "team_mode.index_in_team"
        private const val TEAM_MODE_TEAM_SIZE = "team_mode.team_size"

        // application version
        private const val LAST_VERSION = "lastVersion"
        private const val LAST_VERSION_DATA = "lastVersion_data"

        // main screen UI
        private const val HAS_SHOWN_TUTORIAL = "hasShownTutorial"
        private const val HAS_SHOWN_OVERLAYS_TUTORIAL = "hasShownOverlaysTutorial"
        private const val QUEST_SELECTION_HINT_STATE = "questSelectionHintState"

        // map state
        private const val MAP_LATITUDE = "map.latitude"
        private const val MAP_LONGITUDE = "map.longitude"
        private const val MAP_ROTATION = "map.rotation2"
        private const val MAP_TILT = "map.tilt2"
        private const val MAP_ZOOM = "map.zoom2"
        private const val MAP_FOLLOWING = "map.following"
        private const val MAP_NAVIGATION_MODE = "map.navigation_mode"

        // clean-up after upgrade
        private const val CLEARED_TANGRAM_CACHE = "cleared_tangram_cache"

        // quest & overlays
        private const val PREFERRED_LANGUAGE_FOR_NAMES = "preferredLanguageForNames"
        private const val SELECTED_QUESTS_PRESET = "selectedQuestsPreset"
        private const val SELECTED_OVERLAY = "selectedOverlay"
        private const val LAST_PICKED_PREFIX = "imageListLastPicked."
        private const val LAST_EDIT_TIME = "changesets.lastChangeTime"

        // profile & statistics screen UI
        private const val USER_DAYS_ACTIVE = "days_active"
        private const val USER_GLOBAL_RANK = "user_global_rank"
        private const val USER_GLOBAL_RANK_CURRENT_WEEK = "user_global_rank_current_week"
        private const val USER_LAST_TIMESTAMP_ACTIVE = "last_timestamp_active"
        private const val ACTIVE_DATES_RANGE = "active_days_range"
        private const val IS_SYNCHRONIZING_STATISTICS = "is_synchronizing_statistics"
    }
}
