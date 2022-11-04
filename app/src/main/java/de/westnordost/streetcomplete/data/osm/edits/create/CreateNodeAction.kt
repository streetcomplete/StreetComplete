package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that creates a (free-floating) node. */
@Serializable
data class CreateNodeAction(
    val position: LatLon,
    val tags: Map<String, String>
) : ElementEditAction, IsActionRevertable {

    override val newElementsCount get() = NewElementsCount(1, 0, 0)

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val node = Node(idProvider.nextNodeId(), position, tags, 1, nowAsEpochMilliseconds())
        return MapDataChanges(creations = listOf(node))
    }

    override fun createReverted(): ElementEditAction = RevertCreateNodeAction
}
