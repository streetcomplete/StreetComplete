package de.westnordost.streetcomplete.data.osm.edits.move

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action reverts moving a node. */
@Serializable
object RevertMoveNodeAction : ElementEditAction, IsRevertAction {

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val node = element as? Node ?: throw ConflictException("Element deleted")
        return MapDataChanges(modifications = listOf(node.copy(
            position = (originalElement as Node).position,
            timestampEdited = nowAsEpochMilliseconds()
        )))
    }
}
