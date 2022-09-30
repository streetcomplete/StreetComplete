package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource

class CacheTrimmer(
    private val visibleQuestsSource: VisibleQuestsSource,
    private val mapDataController: MapDataController,
) {
    fun trimCaches() {
        mapDataController.trimCache()
        visibleQuestsSource.trimCache()
    }

    fun clearCaches() {
        mapDataController.clearCache()
        visibleQuestsSource.clearCache()
    }
}
