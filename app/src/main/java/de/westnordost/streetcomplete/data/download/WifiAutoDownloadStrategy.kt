package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController

/** Download strategy if user is on wifi */
class WifiAutoDownloadStrategy(
    mapDataController: MapDataController,
    downloadedTilesDao: DownloadedTilesDao
) : AVariableRadiusStrategy(mapDataController, downloadedTilesDao) {

    /** Let's assume that if the user is on wifi, he is either at home, at work, in the hotel, at a
     * café,... in any case, somewhere that would act as a "base" from which he can go on an
     * excursion. Let's make sure he can, even if there is no or bad internet.
     */

    override val maxDownloadAreaInKm2 = 20.0 // that's a radius of about 2.5 km
    override val desiredScoredMapDataCountInVicinity = 15000
}
