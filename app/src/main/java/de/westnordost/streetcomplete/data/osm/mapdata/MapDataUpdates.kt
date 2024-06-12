package de.westnordost.streetcomplete.data.osm.mapdata

/** Data class that contains the map data updates (updated elements, deleted elements, elements
 *  whose id have been updated) after the modifications have been uploaded */
data class MapDataUpdates(
    val updated: Collection<Element> = emptyList(),
    val deleted: Collection<ElementKey> = emptyList(),
    val idUpdates: Collection<ElementIdUpdate> = emptyList()
)

data class ElementIdUpdate(
    val elementType: ElementType,
    val oldElementId: Long,
    val newElementId: Long
)

fun createMapDataUpdates(
    elements: Collection<Element>,
    updates: Map<ElementKey, ElementUpdateAction>,
    ignoreRelationTypes: Set<String?> = emptySet()
): MapDataUpdates {
    val updatedElements = mutableListOf<Element>()
    val deletedElementKeys = mutableListOf<ElementKey>()
    val idUpdates = mutableListOf<ElementIdUpdate>()

    for (element in elements) {
        if (element is Relation && element.tags["type"] in ignoreRelationTypes) continue

        val newElement = element.update(updates)
        if (newElement == null) {
            deletedElementKeys.add(element.key)
        } else if (newElement !== element) {
            updatedElements.add(newElement)
            if (element.id != newElement.id) {
                idUpdates.add(ElementIdUpdate(element.type, element.id, newElement.id))
            }
        }
    }

    return MapDataUpdates(updatedElements, deletedElementKeys, idUpdates)
}

/**
 * Apply the given updates to this element.
 *
 * @return null if the element was deleted, this if nothing was changed or an updated element
 *         if anything way changed, e.g. the element's version or id, but also if any way node or
 *         relation member('s id) was changed */
private fun Element.update(updates: Map<ElementKey, ElementUpdateAction>): Element? {
    val update = updates[key]
    if (update is DeleteElement) return null
    val u = update as UpdateElement? // kotlin doesn't infer this
    return when (this) {
        is Node -> update(u)
        is Relation -> update(u, updates)
        is Way -> update(u, updates)
    }
}

private fun Node.update(update: UpdateElement?): Node {
    return if (update != null) copy(id = update.newId, version = update.newVersion) else this
}

private fun Way.update(update: UpdateElement?, updates: Map<ElementKey, ElementUpdateAction>): Way {
    val newNodeIds = nodeIds.mapNotNull { nodeId ->
        when (val nodeUpdate = updates[ElementKey(ElementType.NODE, nodeId)]) {
            DeleteElement -> null
            is UpdateElement -> nodeUpdate.newId
            null -> nodeId
        }
    }
    if (newNodeIds == nodeIds && update == null) return this
    return copy(
        id = update?.newId ?: id,
        version = update?.newVersion ?: version,
        nodeIds = newNodeIds
    )
}

private fun Relation.update(update: UpdateElement?, updates: Map<ElementKey, ElementUpdateAction>): Relation {
    val newMembers = members.mapNotNull { member ->
        when (val memberUpdate = updates[ElementKey(member.type, member.ref)]) {
            DeleteElement -> null
            is UpdateElement -> member.copy(ref = memberUpdate.newId)
            null -> member
        }
    }
    if (newMembers == members && update == null) return this
    return copy(
        id = update?.newId ?: id,
        version = update?.newVersion ?: version,
        members = newMembers
    )
}
