package de.westnordost.streetcomplete.data.download

import javax.inject.Inject
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider

class MobileDataAutoDownloadStrategy @Inject constructor(
    osmQuestDB: OsmQuestDao,
    downloadedTilesDao: DownloadedTilesDao,
    questTypesProvider: OrderedVisibleQuestTypesProvider
) : AActiveRadiusStrategy(osmQuestDB, downloadedTilesDao, questTypesProvider) {

    override val questTypeDownloadCount = 4
    override val minQuestsInActiveRadiusPerKm2 = 8
    override val activeRadii = intArrayOf(300)
    override val downloadRadius = 600
}
