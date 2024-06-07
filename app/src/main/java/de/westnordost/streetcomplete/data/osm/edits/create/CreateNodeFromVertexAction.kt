package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.edits.update_tags.RevertUpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.changesApplied
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlinx.serialization.Serializable

/** Action that transforms a way vertex into a node, i.e. adds tags to a vertex in a way.
 *
 *  A node in a way (a vertex) always serves to define the geometry of a way, regardless whether
 *  it has tags on its own and thus describes an own feature or not. In other words, the meaning of
 *  a vertex is always strongly bound to its location and membership in way(s), exclusively so if
 *  the vertex has no tags at all: If we add tags to a vertex without tags, it is like creating a
 *  new node - not on the technical level, but on the semantic level.
 *
 *  The user intention represented by this action is to add meaning (=tags) to a vertex at a certain
 *  position and membership in certain way(s). So, when this is edit is applied, it is made sure
 *  that the vertex didn't change its meaning in the definition of the geometry: I.e. its position
 *  and the ways it is part of.
 *
 *  So, a conflict will be thrown if:
 *
 *  * the node has been moved at all. Vertices on ways are frequently moved around to adapt
 *    the geometry of the way. If we want to add a crossing at a certain location and someone else
 *    moved the way or some vertices, there is no telling if the position of the would-be crossing
 *    would still be correct
 *
 *  * the node is not contained in exactly the same ways when applying the edit as when the edit
 *    was created. If this changed, e.g. the intersection may have been moved elsewhere, which would
 *    mean that whatever tags we wanted to add to this node might have to go elsewhere too
 *
 *  */
@Serializable
data class CreateNodeFromVertexAction(
    val originalNode: Node,
    val changes: StringMapChanges,
    val containingWayIds: List<Long>,
) : ElementEditAction, IsActionRevertable {

    // no new node is created, just a vertex (which is also a node) gets some tags now
    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override val elementKeys get() =
        containingWayIds.map { ElementKey(ElementType.WAY, it) } + originalNode.key

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        originalNode = originalNode.copy(id = updatedIds[originalNode.key] ?: originalNode.id),
        containingWayIds = containingWayIds.map { updatedIds[ElementKey(ElementType.WAY, it)] ?: it }
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val currentNode = mapDataRepository.getNode(originalNode.id)
            ?: throw ConflictException("Element deleted")

        if (originalNode.position != currentNode.position) {
            throw ConflictException("Node position changed")
        }

        val currentContainingWayIds = mapDataRepository.getWaysForNode(originalNode.id).map { it.id }
        if (!currentContainingWayIds.containsExactlyInAnyOrder(containingWayIds)) {
            throw ConflictException("Node is not part of exactly the same ways as before")
        }

        return MapDataChanges(modifications = listOf(currentNode.changesApplied(changes)))
    }

    override fun createReverted(idProvider: ElementIdProvider) =
        // the reverse is a normal revert of the element tags, because we (potentially) return
        // FROM a node with meaning to a vertex that only has meaning for geometry
        RevertUpdateElementTagsAction(originalNode, changes.reversed())
}
