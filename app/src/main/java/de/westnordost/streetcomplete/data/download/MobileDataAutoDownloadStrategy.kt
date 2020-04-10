package de.westnordost.streetcomplete.data.download

import javax.inject.Inject
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider

/** Download strategy if user is on mobile data */
class MobileDataAutoDownloadStrategy @Inject constructor(
    visibleQuestsSource: VisibleQuestsSource,
    downloadedTilesDao: DownloadedTilesDao,
    questTypesProvider: OrderedVisibleQuestTypesProvider
) : AActiveRadiusStrategy(visibleQuestsSource, downloadedTilesDao, questTypesProvider) {

    override val questTypeDownloadCount = 4
    override val minQuestsInActiveRadiusPerKm2 = 8
    override val activeRadii = intArrayOf(300)
    override val downloadRadius = 600
}
