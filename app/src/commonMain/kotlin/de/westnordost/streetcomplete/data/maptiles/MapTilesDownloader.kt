package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

/** Downloads map tiles for offline use */
interface MapTilesDownloader {
    /** Fetch tiles in the given [bbox] */
    suspend fun download(bbox: BoundingBox)

    /** Delete those tiles that are older than the given [time] */
    suspend fun deleteOld(time: Long)

    /** Delete all previously fetched tiles */
    suspend fun clear()

}
