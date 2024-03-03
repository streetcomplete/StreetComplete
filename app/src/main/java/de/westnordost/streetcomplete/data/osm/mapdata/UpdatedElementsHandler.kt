package de.westnordost.streetcomplete.data.osm.mapdata

/** Reads the answer of an update map call on the OSM API. */
class UpdatedElementsHandler(val ignoreRelationTypes: Set<String?> = emptySet()) {
    private val nodeDiffs: MutableMap<Long, DiffElement> = mutableMapOf()
    private val wayDiffs: MutableMap<Long, DiffElement> = mutableMapOf()
    private val relationDiffs: MutableMap<Long, DiffElement> = mutableMapOf()

    fun handle(d: DiffElement) {
        when (d.type) {
            ElementType.NODE -> nodeDiffs[d.clientId] = d
            ElementType.WAY -> wayDiffs[d.clientId] = d
            ElementType.RELATION -> relationDiffs[d.clientId] = d
        }
    }

    fun getElementUpdates(elements: Collection<Element>): MapDataUpdates {
        val updatedElements = mutableListOf<Element>()
        val deletedElementKeys = mutableListOf<ElementKey>()
        val idUpdates = mutableListOf<ElementIdUpdate>()
        for (element in elements) {
            if (element is Relation && element.tags["type"] in ignoreRelationTypes) {
                continue
            }
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
        return MapDataUpdates(updatedElements, deletedElementKeys, idUpdates)
    }

    private fun getDiff(type: ElementType, id: Long): DiffElement? = when (type) {
        ElementType.NODE -> nodeDiffs[id]
        ElementType.WAY -> wayDiffs[id]
        ElementType.RELATION -> relationDiffs[id]
    }

    private fun createUpdatedElement(element: Element, newId: Long, newVersion: Int): Element =
        when (element) {
            is Node -> createUpdatedNode(element, newId, newVersion)
            is Way -> createUpdatedWay(element, newId, newVersion)
            is Relation -> createUpdatedRelation(element, newId, newVersion)
        }

    private fun createUpdatedNode(node: Node, newId: Long, newVersion: Int): Node =
        Node(newId, node.position, HashMap(node.tags), newVersion, node.timestampEdited)

    private fun createUpdatedWay(way: Way, newId: Long, newVersion: Int): Way {
        val newNodeIds = ArrayList<Long>(way.nodeIds.size)
        for (nodeId in way.nodeIds) {
            val diff = nodeDiffs[nodeId]
            if (diff == null) {
                newNodeIds.add(nodeId)
            } else if (diff.serverId != null) {
                newNodeIds.add(diff.serverId)
            }
        }
        return Way(newId, newNodeIds, HashMap(way.tags), newVersion, way.timestampEdited)
    }

    private fun createUpdatedRelation(relation: Relation, newId: Long, newVersion: Int): Relation {
        val newRelationMembers = ArrayList<RelationMember>(relation.members.size)
        for (member in relation.members) {
            val diff = getDiff(member.type, member.ref)
            if (diff == null) {
                newRelationMembers.add(RelationMember(member.type, member.ref, member.role))
            } else if (diff.serverId != null) {
                newRelationMembers.add(RelationMember(member.type, diff.serverId, member.role))
            }
        }
        return Relation(newId, newRelationMembers, HashMap(relation.tags), newVersion, relation.timestampEdited)
    }
}
