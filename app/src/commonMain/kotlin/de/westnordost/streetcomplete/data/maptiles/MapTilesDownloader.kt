package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

interface MapTilesDownloader {
    suspend fun download(bbox: BoundingBox)
    suspend fun clear()
}
