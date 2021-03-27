package de.westnordost.osmapi.map

import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.changes.DiffElement
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

/** Reads the answer of an update map call on the OSM API. */
class UpdatedElementsHandler : Handler<DiffElement> {
    private val nodeDiffs: MutableMap<Long, DiffElement> = mutableMapOf()
    private val wayDiffs: MutableMap<Long, DiffElement> = mutableMapOf()
    private val relationDiffs: MutableMap<Long, DiffElement> = mutableMapOf()

    override fun handle(d: DiffElement) {
        when (d.type ?: return) {
            Element.Type.NODE -> nodeDiffs[d.clientId] = d
            Element.Type.WAY -> wayDiffs[d.clientId] = d
            Element.Type.RELATION -> relationDiffs[d.clientId] = d
        }
    }

    fun getElementUpdates(elements: Collection<Element>): ElementUpdates {
        val updatedElements = mutableListOf<Element>()
        val deletedElementKeys = mutableListOf<ElementKey>()
        val idUpdates = mutableListOf<ElementIdUpdate>()
        for (element in elements) {
            val diff = getDiff(element.type, element.id) ?: continue
            if (diff.serverId != null && diff.serverVersion != null) {
                updatedElements.add(createUpdatedElement(element, diff.serverId, diff.serverVersion))
            } else {
                deletedElementKeys.add(ElementKey(diff.type, diff.clientId))
            }
            if (diff.clientId != diff.serverId && diff.serverId != null) {
                idUpdates.add(ElementIdUpdate(diff.type, diff.clientId, diff.serverId))
            }
        }
        return ElementUpdates(updatedElements, deletedElementKeys, idUpdates)
    }

    private fun getDiff(type: Element.Type, id: Long): DiffElement? = when (type) {
        Element.Type.NODE -> nodeDiffs[id]
        Element.Type.WAY -> wayDiffs[id]
        Element.Type.RELATION -> relationDiffs[id]
    }

    private fun createUpdatedElement(element: Element, newId: Long, newVersion: Int): Element =
        when (element) {
            is Node -> createUpdatedNode(element, newId, newVersion)
            is Way -> createUpdatedWay(element, newId, newVersion)
            is Relation -> createUpdatedRelation(element, newId, newVersion)
            else -> throw RuntimeException()
        }

    private fun createUpdatedNode(node: Node, newId: Long, newVersion: Int): Node {
        return OsmNode(newId, newVersion, node.position, node.tags?.let { HashMap(it) }, null, node.dateEdited)
    }

    private fun createUpdatedWay(way: Way, newId: Long, newVersion: Int): Way {
        val newNodeIds = ArrayList<Long>(way.nodeIds.size)
        for (nodeId in way.nodeIds) {
            val diff = nodeDiffs[nodeId]
            if (diff == null) newNodeIds.add(nodeId)
            else if (diff.serverId != null) newNodeIds.add(diff.serverId)
        }
        return OsmWay(newId, newVersion, newNodeIds, way.tags?.let { HashMap(it) }, null, way.dateEdited)
    }

    private fun createUpdatedRelation(relation: Relation, newId: Long, newVersion: Int): Relation {
        val newRelationMembers = ArrayList<RelationMember>(relation.members.size)
        for (member in relation.members) {
            val diff = getDiff(member.type, member.ref)
            if (diff == null) newRelationMembers.add(OsmRelationMember(member.ref, member.role, member.type))
            else if(diff.serverId != null) newRelationMembers.add(OsmRelationMember(diff.serverId, member.role, member.type))
        }
        return OsmRelation(newId, newVersion, newRelationMembers, relation.tags?.let { HashMap(it) }, null, relation.dateEdited)
    }
}

data class ElementUpdates(
    val updated: Collection<Element> = emptyList(),
    val deleted: Collection<ElementKey> = emptyList(),
    val idUpdates: Collection<ElementIdUpdate> = emptyList()
)

data class ElementIdUpdate(
    val elementType: Element.Type,
    val oldElementId: Long,
    val newElementId: Long
)
