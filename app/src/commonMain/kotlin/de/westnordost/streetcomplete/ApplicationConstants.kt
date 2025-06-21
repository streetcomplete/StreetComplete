package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import kotlin.time.Duration.Companion.minutes

object ApplicationConstants {
    const val NAME = "StreetComplete"
    val USER_AGENT = NAME + " " + BuildConfig.VERSION_NAME
    const val QUESTTYPE_TAG_KEY = NAME + ":quest_type"

    const val OLD_DATABASE_NAME = "streetcomplete.db"
    const val DATABASE_NAME = "streetcomplete_v2.db"

    const val MAX_DOWNLOADABLE_AREA_IN_SQKM = 12.0
    const val MIN_DOWNLOADABLE_AREA_IN_SQKM = 0.1

    /** Android notification channel name and id */
    const val NOTIFICATIONS_CHANNEL_SYNC = "downloading"
    // name is "downloading" for historic reasons, not sure if it has any side-effects if it is changed now
    const val NOTIFICATIONS_ID_SYNC = 1

    const val STREETMEASURE = "de.westnordost.streetmeasure"

    /** tile zoom at which the app downloads automatically and remembers which tiles have already
     *  been downloaded */
    const val DOWNLOAD_TILE_ZOOM = 16

    /** a "best before" duration for downloaded data. OSM data, notes, a tile will not be
     *  downloaded again before the time expired  */
    const val REFRESH_DATA_AFTER = 12L * 60 * 60 * 1000 // 12 hours in ms

    /** the duration after which OSM data, notes, quest meta data etc. will be deleted from the
     *  database if not used anymore and have not been refreshed in the meantime  */
    const val DELETE_OLD_DATA_AFTER = 14L * 24 * 60 * 60 * 1000 // 14 days in ms

    /** the duration after which logs will be deleted from the database */
    const val DELETE_OLD_LOG_AFTER = 14L * 24 * 60 * 60 * 1000 // 14 days in ms

    /** the duration after which logs won't be attached to the crash report */
    const val DO_NOT_ATTACH_LOG_TO_CRASH_REPORT_AFTER = 5L * 60 * 1000 // 5 minutes in ms

    /** Time to wait until a changeset is closed after no more edits are added to that changeset */
    const val CLOSE_CHANGESETS_AFTER_INACTIVITY_OF = 20L * 60 * 1000 // 20min

    /** Maximum distance to the location of the edit last added to the current changeset for it to
     *  be included in that same changeset. Otherwise, a new changeset will be opened for that */
    const val CHANGESET_MAX_LAST_EDIT_DISTANCE = 5000 // 5km

    /** minimum map zoom before allowing to create a note */
    const val NOTE_MIN_ZOOM = 15

    /** Maximum distance the user's GPS location can be situated from the element he wants to edit
     *  for it to be automatically considered a survey.
     *
     *  The distance is the minimum distance between the element geometry (e.g. a road) and the
     *  track he left in the last [MAX_RECENT_LOCATIONS_AGE] duration, minus the GPS (in)accuracy
     *  in meters for each point in that track.
     *
     *  Users should be encouraged to *really* go right there and check even if they think they
     *  see it from afar already */
    const val MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80.0 // m
    val MAX_RECENT_LOCATIONS_AGE = 10.minutes

    /** when new quests that are appearing due to download of an area, show the hint that he can
     *  disable quests in the settings if more than X quests did appear */
    const val QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT = 600

    /** the max age of the undo history - one cannot undo changes older than X  */
    const val MAX_UNDO_HISTORY_AGE = 12L * 60 * 60 * 1000 // 12 hours in ms

    /** The creation of some quests is dependent on surrounding geometry. This constant describes
     *  the maximum distance surrounding elements may affect whether a quest is created or not */
    //  e.g. AddRecyclingContainerMaterials, AddCycleway
    const val QUEST_FILTER_PADDING = 20.0 // m

    /** ATP quest gets a larger padding
     *  In some cases ATP entries are near OSM objects, but offset is larger than QUEST_FILTER_PADDING
     *  Note that download still uses QUEST_FILTER_PADDING, that should be fine overall
     */
    const val ATP_QUEST_FILTER_PADDING = 50.0 // m
    const val AVATARS_CACHE_DIRECTORY = "osm_user_avatars"

    const val SC_PHOTO_SERVICE_URL = "https://streetcomplete.app/photo-upload/" // must have trailing /

    const val ATTACH_PHOTO_QUALITY = 65 // doesn't need to look super pretty
    const val ATTACH_PHOTO_MAX_SIZE = 1920 // Full HD

    // where to send the error reports to
    const val ERROR_REPORTS_EMAIL = "streetcomplete_errors@westnordost.de"

    /** Which relation types to drop already during download, before persisting. This is a
     *  performance improvement. Working properly with relations means we have to have it as
     *  complete as possible. Some relations are extremely large, which would require to pull
     *  a lot of elements from db into memory. */
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
