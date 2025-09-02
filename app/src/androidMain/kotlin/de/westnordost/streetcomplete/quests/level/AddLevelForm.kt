package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace

open class AddLevelForm : AAddLevelForm() {
     override suspend fun filter(mapData: MapDataWithGeometry): List<Element> = mapData.filter {
        // The AddLevel quest only shows places on the same level, while the AddLevelThing quest
        // shows Things AND Places

        it.tags["level"] != null && it.isPlaceOrDisusedPlace()
    }
}
