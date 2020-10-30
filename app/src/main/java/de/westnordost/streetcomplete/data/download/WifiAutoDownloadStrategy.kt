package de.westnordost.streetcomplete.data.download


import javax.inject.Inject

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource

/** Download strategy if user is on wifi */
class WifiAutoDownloadStrategy @Inject constructor(
    visibleQuestsSource: VisibleQuestsSource,
    downloadedTilesDao: DownloadedTilesDao
) : AVariableRadiusStrategy(visibleQuestsSource, downloadedTilesDao) {

    /** Let's assume that if the user is on wifi, he is either at home, at work, in the hotel, at a
     * café,... in any case, somewhere that would act as a "base" from which he can go on an
     * excursion. Let's make sure he can, even if there is no or bad internet.
     */

    override val maxDownloadAreaInKm2 = 12.0 // that's a radius of about 2 km
    override val desiredQuestCountInVicinity = 1000
}
