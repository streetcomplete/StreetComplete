package de.westnordost.streetcomplete.data.osm.mapdata

// TODO this class must be replaced + tests

/** Reads the answer of an update map call on the OSM API. */
class UpdatedElementsHandler() {
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

    fun getElementUpdates(
        elements: Collection<Element>,
        ignoreRelationTypes: Set<String?> = emptySet()
    ): MapDataUpdates {
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
            is Node -> element.copy(id = newId, version = newVersion)
            is Way -> element.copy(
                id = newId,
                nodeIds = element.nodeIds.map { clientId ->
                    val serverId = nodeDiffs[clientId]?.serverId
                    serverId ?: clientId
                },
                version = newVersion
            )
            is Relation -> element.copy(
                id = newId,
                members = element.members.map { member ->
                    val serverId = getDiff(member.type, member.ref)?.serverId
                    serverId?.let { member.copy(ref = it) } ?: member
                },
                version = newVersion
            )
        }
}
