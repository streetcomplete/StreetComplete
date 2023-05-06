package de.westnordost.streetcomplete.data.download.strategy

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController

/** Download strategy if user is on mobile data */
class MobileDataAutoDownloadStrategy(
    mapDataController: MapDataController,
    downloadedTilesSource: DownloadedTilesSource
) : AVariableRadiusStrategy(mapDataController, downloadedTilesSource) {

    override val maxDownloadAreaInKm2 = 10.0 // that's a radius of about 1.5 km
    override val desiredScoredMapDataCountInVicinity = 7500
}
