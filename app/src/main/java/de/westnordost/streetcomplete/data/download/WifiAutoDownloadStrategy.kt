package de.westnordost.streetcomplete.data.download


import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider

class WifiAutoDownloadStrategy @Inject constructor(
    osmQuestDB: OsmQuestDao, downloadedTilesDao: DownloadedTilesDao,
    questTypes: OrderedVisibleQuestTypesProvider
) : AActiveRadiusStrategy(osmQuestDB, downloadedTilesDao, questTypes) {

    /** Let's assume that if the user is on wifi, he is either at home, at work, in the hotel, at a
     * café,... in any case, somewhere that would act as a "base" from which he can go on an
     * excursion. Let's make sure he can, even if there is no or bad internet.
     *
     * Since download size is almost unlimited, we can be very generous here.
     * However, Overpass is as limited as always, so the number of quest types we download is
     * limited as before  */

    override val questTypeDownloadCount = 8
    override val minQuestsInActiveRadiusPerKm2 = 12

    // checks if either in 600 or 300m radius, there are enough quests.
    override val activeRadii = intArrayOf(600, 300)
    override val downloadRadius = 1200
}
