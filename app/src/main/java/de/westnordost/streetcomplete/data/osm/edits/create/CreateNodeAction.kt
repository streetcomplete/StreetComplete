package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
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

    override val elementKeys get() = emptyList<ElementKey>()

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val newNode = Node(idProvider.nextNodeId(), position, tags, 1, nowAsEpochMilliseconds())
        return MapDataChanges(creations = listOf(newNode))
    }

    override fun createReverted(idProvider: ElementIdProvider) =
        RevertCreateNodeAction(
            Node(idProvider.nextNodeId(), position, tags, 1, nowAsEpochMilliseconds())
        )
}
