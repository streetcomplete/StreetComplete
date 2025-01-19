package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction

object ApplicationConstants {
    const val NAME = "StreetComplete_ee"
    const val USER_AGENT = NAME + " " + BuildConfig.VERSION_NAME
    const val QUESTTYPE_TAG_KEY = "StreetComplete:quest_type" // use original SC here, so statistics are counted

    const val MAX_DOWNLOADABLE_AREA_IN_SQKM = 12.0
    const val MIN_DOWNLOADABLE_AREA_IN_SQKM = 0.1

    const val DATABASE_NAME = "streetcomplete_v2.db"
    const val OLD_DATABASE_NAME = "streetcomplete.db"

    /** tile zoom at which the app downloads automatically and remembers which tiles have already
     *  been downloaded */
    const val DOWNLOAD_TILE_ZOOM = 16

    /** a "best before" duration for downloaded data. OSM data, notes, a tile will not be
     *  downloaded again before the time expired  */
    const val REFRESH_DATA_AFTER = 12L * 60 * 60 * 1000 // 12 hours in ms

    /** the duration after which OSM data, notes, quest meta data etc. will be deleted from the
     *  database if not used anymore and have not been refreshed in the meantime  */
    const val DELETE_OLD_DATA_AFTER_DAYS = 14

    /** the duration after which logs will be deleted from the database */
    const val DELETE_OLD_LOG_AFTER = 14L * 24 * 60 * 60 * 1000 // 14 days in ms

    /** the duration after which logs won't be attached to the crash report */
    const val DO_NOT_ATTACH_LOG_TO_CRASH_REPORT_AFTER = 5L * 60 * 1000 // 5 minutes in ms

    const val NOTE_MIN_ZOOM = 15

    /** default maximum zoom for satellite imagery */
    const val RASTER_DEFAULT_MAXZOOM = 21
    const val RASTER_DEFAULT_URL = "https://server.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}?blankTile=false"

    /** when new quests that are appearing due to download of an area, show the hint that he can
     *  disable quests in the settings if more than X quests did appear */
    const val QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT = 600

    /** the max age of the undo history - one cannot undo changes older than X  */
    const val MAX_UNDO_HISTORY_AGE = 12L * 60 * 60 * 1000 // 12 hours in ms

    /** The creation of some quests is dependent on surrounding geometry. This constant describes
     *  the maximum distance surrounding elements may affect whether a quest is created or not */
    //  e.g. AddRecyclingContainerMaterials, AddCycleway
    const val QUEST_FILTER_PADDING = 20.0 // m

    const val AVATARS_CACHE_DIRECTORY = "osm_user_avatars"

    const val SC_PHOTO_SERVICE_URL = "https://streetcomplete.app/photo-upload/" // must have trailing /

    const val ATTACH_PHOTO_QUALITY = 65 // doesn't need to look super pretty
    const val ATTACH_PHOTO_MAX_SIZE = 1920 // Full HD

    // name is "downloading" for historic reasons, not sure if it has any side-effects if it is changed now
    const val NOTIFICATIONS_CHANNEL_SYNC = "downloading"
    const val NOTIFICATIONS_ID_SYNC = 1

    // where to send the error reports to
    const val ERROR_REPORTS_EMAIL = "helium@vivaldi.net"

    const val STREETMEASURE = "de.westnordost.streetmeasure"

    val IGNORED_RELATION_TYPES = setOf(
        // could be useful, but sometimes/often very very large
        "route", "route_master", "superroute", "network", "disused:route",
        // very large, not useful for SC
        "boundary",
        // can easily span very large areas, not useful for SC
        "water", "waterway", "watershed", "collection",
        // questionable relation type: members could easily span multiple continents
        "person",
        // no wiki entry, sounds like it could span large areas
        "power", "pipeline", "railway"
    )

    val EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES = setOf(
        /* because this action may edit route relations but route relations are not persisted
           locally for performance reasons */
        SplitWayAction::class
    )

    const val EE_QUEST_OFFSET = 2222 // must be larger than the largest SC ordinal, and should not be changed to allow preset transfer

    /*
    During development it might be better to work against the Test-API, rather than the
    Live-API. Developers are reminded that they need a separate login for the Test-API and
    can register/logon via https://master.apis.dev.openstreetmap.org/
    Note that test actions not applied to the database do not require test API (test edits that are reverted
    locally before an upload etc) and that test API has a separate database that is mostly empty
    (test data needs to be created there).
     */
    const val USE_TEST_API = false
}
