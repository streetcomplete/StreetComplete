package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.changes.DiffElement
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.ElementKey

/** Reads the answer of an update map call on the OSM API. */
class UpdateElementsHandler : Handler<DiffElement> {
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
        for (element in elements) {
            val update = getDiff(element.type, element.id) ?: continue
            if (update.serverId != null && update.serverVersion != null) {
                updatedElements.add(createUpdatedElement(element, update.serverId, update.serverVersion))
            } else {
                deletedElementKeys.add(ElementKey(update.type, update.clientId))
            }
        }
        return ElementUpdates(updatedElements, deletedElementKeys)
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
        return OsmNode(newId, newVersion, node.position, node.tags?.let { HashMap(it) })
    }

    private fun createUpdatedWay(way: Way, newId: Long, newVersion: Int): Way {
        val newNodeIds = ArrayList<Long>(way.nodeIds.size)
        for (nodeId in way.nodeIds) {
            val update = nodeDiffs[nodeId]
            if (update == null) newNodeIds.add(nodeId)
            else if (update.serverId != null) newNodeIds.add(update.serverId)
        }
        return OsmWay(newId, newVersion, newNodeIds, way.tags?.let { HashMap(it) })
    }

    private fun createUpdatedRelation(relation: Relation, newId: Long, newVersion: Int): Relation {
        val newRelationMembers = ArrayList<RelationMember>(relation.members.size)
        for (member in relation.members) {
            val update = getDiff(member.type, member.ref)
            if (update == null) newRelationMembers.add(OsmRelationMember(member.ref, member.role, member.type))
            else if(update.serverId != null) newRelationMembers.add(OsmRelationMember(update.serverId, member.role, member.type))
        }
        return OsmRelation(newId, newVersion, newRelationMembers, relation.tags?.let { HashMap(it) })
    }
}

data class ElementUpdates(val updated: Collection<Element>, val deleted: Collection<ElementKey>)
