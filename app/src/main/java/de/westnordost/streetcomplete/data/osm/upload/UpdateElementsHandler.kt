package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.changes.DiffElement
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.ElementKey
import de.westnordost.streetcomplete.ktx.copy
import kotlin.collections.HashSet

/** Reads the answer of an update map call on the OSM API and updates the given elements with the
 *  DiffElement response.
 *  The updatedElements deletedElementsKeys sets contain the elements that have been changed as
 *  a result of the update. */
class UpdateElementsHandler(val elements: MutableCollection<Element>) : Handler<DiffElement> {
    val updatedElements: MutableSet<Element> = HashSet()
    val deletedElementsKeys: MutableSet<ElementKey> = HashSet()

    private val relations get() = elements.filterIsInstance<Relation>()
    private val ways get() = elements.filterIsInstance<Way>()

    override fun handle(d: DiffElement) {
        val element = elements.find { it.type == d.type && it.id == d.clientId } ?: return
        if (d.serverVersion == null || d.serverId == null) {
            deleteElement(element.type, element.id)
        }
        else if (element.version != d.serverVersion || element.id != d.serverId) {
            updateElement(element, d.serverId, d.serverVersion)
        }
    }

    private fun deleteElement(type: Element.Type, id: Long) {
        deletedElementsKeys.add(ElementKey(type, id))

        if (type == Element.Type.NODE) {
            deleteDeletedNodesFromWays(id)
        }
        deleteDeletedElementsFromRelations(type, id)
    }

    private fun deleteDeletedNodesFromWays(id: Long) {
        for (way in ways) {
            val it = way.nodeIds.listIterator()
            while (it.hasNext()) {
                if (it.next() == id) {
                    it.remove()
                    updatedElements.add(way)
                }
            }
        }
    }

    private fun deleteDeletedElementsFromRelations(type: Element.Type, id: Long) {
        for (relation in relations) {
            val it = relation.members.listIterator()
            while (it.hasNext()) {
                val member = it.next()
                if (member.type == type && member.ref == id) {
                    it.remove()
                    updatedElements.add(relation)
                }
            }
        }
    }

    private fun updateElement(element: Element, newId: Long, newVersion: Int) {
        val oldId = element.id
        val newElement = element.copy(newId, newVersion)
        updatedElements.add(newElement)

        if (element.type == Element.Type.NODE) {
            updateWaysWithUpdatedNodeId(oldId, newId)
        }
        updateRelationsWithUpdatedElementId(element.type, oldId, newId)
    }

    private fun updateWaysWithUpdatedNodeId(oldId: Long, newId: Long) {
        for (way in ways) {
            val it = way.nodeIds.listIterator()
            while (it.hasNext()) {
                if (it.next() == oldId) {
                    it.set(newId)
                    updatedElements.add(way)
                }
            }
        }
    }

    private fun updateRelationsWithUpdatedElementId(type: Element.Type, oldId: Long, newId: Long) {
        for (relation in relations) {
            val it = relation.members.listIterator()
            while (it.hasNext()) {
                val member = it.next()
                if (member.type == type && member.ref == oldId) {
                    it.set(OsmRelationMember(newId, member.role, type))
                    updatedElements.add(relation)
                }
            }
        }
    }
}

