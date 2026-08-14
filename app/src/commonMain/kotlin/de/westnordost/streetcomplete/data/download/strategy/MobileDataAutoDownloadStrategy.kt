package de.westnordost.streetcomplete.data.download.strategy

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataSource

/** Download strategy if user is on mobile data */
class MobileDataAutoDownloadStrategy(
    mapDataSource: MapDataSource,
    downloadedTilesSource: DownloadedTilesSource
) : AVariableRadiusStrategy(mapDataSource, downloadedTilesSource) {

    override val maxDownloadAreaInKm2 = 10.0 // that's a radius of about 1.5 km
    override val desiredScoredMapDataCountInVicinity = 7500
}
