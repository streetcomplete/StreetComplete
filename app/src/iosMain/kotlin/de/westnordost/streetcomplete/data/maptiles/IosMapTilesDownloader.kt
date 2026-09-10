package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

class IosMapTilesDownloader : MapTilesDownloader {
    override suspend fun download(bbox: BoundingBox) {}
    override suspend fun deleteOld(time: Long) {}
    override suspend fun clear() {}
}
