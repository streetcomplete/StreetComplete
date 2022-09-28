package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import kotlinx.serialization.Serializable

/** Action reverts creation of a (free-floating) node.
 *
 *  If the node has been touched at all in the meantime (node moved or tags changed), there'll be
 *  a conflict. */
@Serializable
object RevertCreateNodeAction : ElementEditAction, IsRevertAction {

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val node = element as? Node ?: throw ConflictException("Element deleted")
        /* Independent of whether it makes sense or not to check for conflicts on reverting the
           creating (=deleting), it is not possible to check for conflicts between element and
           originalElement (tags changed, position changed) technically:

           On reverting, the "originalElement" from the edit that is being reverted is copied to
           this edit in ElementEditsController::undo. However, that "originalElement" of the
           "CreateNodeAction" is just an empty (dummy) element with no tags since that element did
           not exist yet.

           ElementEditsController would need to use the element as used currently in the app (from
           MapDataWithEditsSource) as basis for the "originalElement" but to not create a cyclic
           dependency, users of EditHistoryController would have to pass in the current element +
           geometry into ElementEditsController::undo.
           Instead, let's just not check for conflicts here.
         */
        return MapDataChanges(deletions = listOf(node))
    }
}
