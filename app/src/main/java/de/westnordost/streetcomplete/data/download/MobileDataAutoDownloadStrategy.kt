package de.westnordost.streetcomplete.data.download

import javax.inject.Inject
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController

/** Download strategy if user is on mobile data */
class MobileDataAutoDownloadStrategy @Inject constructor(
    mapDataController: MapDataController,
    downloadedTilesDao: DownloadedTilesDao
) : AVariableRadiusStrategy(mapDataController, downloadedTilesDao) {

    override val maxDownloadAreaInKm2 = 10.0 // that's a radius of about 1.5 km
    override val desiredScoredMapDataCountInVicinity = 7500
}
