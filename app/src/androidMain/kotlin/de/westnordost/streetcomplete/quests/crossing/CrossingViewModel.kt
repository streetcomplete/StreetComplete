package de.westnordost.streetcomplete.quests.crossing

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import kotlin.text.get

class CrossingViewModel(
    private val mapDataSource: MapDataWithEditsSource,
) : ViewModel() {

    fun isNodeOnSidewalkOrCrossing(node: Node): Boolean =
        mapDataSource.getWaysForNode(node.id).any {
            val footway = it.tags["footway"]
            footway == "sidewalk" || footway == "crossing"
        }
}
