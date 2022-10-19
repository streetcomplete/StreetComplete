package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that restores a POI node to the previous state before deletion/clearing of tags
 */
@Serializable
object RevertDeletePoiNodeAction : ElementEditAction, IsRevertAction {

    /** No "new" elements are created, instead, an old one is being revived */
    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        if (originalElement !is Node) throw ConflictException()

        val newVersion = originalElement.version + 1
        // already has been restored apparently
        if (element != null && element.version > newVersion) {
            throw ConflictException("Element has been restored already")
        }

        val newElement = originalElement.copy(
            version = newVersion,
            timestampEdited = nowAsEpochMilliseconds()
        )
        return MapDataChanges(modifications = listOf(newElement))
    }
}
