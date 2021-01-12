package de.westnordost.streetcomplete

object ApplicationConstants {
    const val NAME = "StreetComplete"
    const val USER_AGENT = NAME + " " + BuildConfig.VERSION_NAME
    const val QUESTTYPE_TAG_KEY = NAME + ":quest_type"

    const val MAX_DOWNLOADABLE_AREA_IN_SQKM = 12.0
    const val MIN_DOWNLOADABLE_AREA_IN_SQKM = 0.1

    const val DATABASE_NAME = "streetcomplete.db"

    const val QUEST_TILE_ZOOM = 16
    const val NOTE_MIN_ZOOM = 15

    /** a "best before" duration for quests. Quests will not be downloaded again for any tile
     * before the time expired  */
    const val REFRESH_QUESTS_AFTER = 3L * 24 * 60 * 60 * 1000 // 3 days in ms

    /** the duration after which quests (and quest meta data) will be deleted from the database if
     * unsolved and not refreshed in the meantime  */
    const val DELETE_UNSOLVED_QUESTS_AFTER = 14L * 24 * 60 * 60 * 1000 // 14 days in ms

    /** the max age of the undo history - one cannot undo changes older than X  */
    const val MAX_QUEST_UNDO_HISTORY_AGE = 24L * 60 * 60 * 1000 // 1 day in ms

    const val AVATARS_CACHE_DIRECTORY = "osm_user_avatars"

    const val SC_PHOTO_SERVICE_URL = "https://westnordost.de/streetcomplete/photo-upload/" // must have trailing /

    const val ATTACH_PHOTO_QUALITY = 80
    const val ATTACH_PHOTO_MAXWIDTH = 1280 // WXGA
    const val ATTACH_PHOTO_MAXHEIGHT = 1280 // WXGA

    const val NOTIFICATIONS_CHANNEL_DOWNLOAD = "downloading"
}
