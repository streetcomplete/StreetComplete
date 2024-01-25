package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource

/** Trims caches in case the memory becomes scarce */
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
