package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.changes.DiffElement
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.NODE
import de.westnordost.osmapi.map.data.Element.Type.WAY
import de.westnordost.osmapi.map.data.Element.Type.RELATION
import org.junit.Assert.*
import org.junit.Test

class UpdateElementsHandlerTest {
    @Test fun `updates element version`() {
        val handler = UpdateElementsHandler()
        handler.handle(diff(NODE, 1, 1, 123))

        val element = handler.getElementUpdates(listOf(node(1))).updated.single()
        assertEquals(123, element.version)
    }

    @Test fun `deletes element`() {
        val handler = UpdateElementsHandler()
        handler.handle(diff(NODE, 1))

        val deletedElementKey = handler.getElementUpdates(listOf(node(1))).deleted.single()
        assertEquals(1, deletedElementKey.elementId)
        assertEquals(NODE, deletedElementKey.elementType)
    }

    @Test fun `updates element id`() {
        val handler = UpdateElementsHandler()
        handler.handle(diff(NODE, -1, 123456, 1))

        val element = handler.getElementUpdates(listOf(node(-1))).updated.single()
        assertEquals(123456, element.id)
    }

    @Test fun `updates node id and all ways containing this id`() {
        val elements = listOf<Element>(
            node(-1),
            way(1, mutableListOf(3,2,-1)), // contains it once
            way(2, mutableListOf(-1,2,-1,-1)), // contains it multiple times
            way(3, mutableListOf(3,4)) // contains it not
        )
        val handler = UpdateElementsHandler()
        handler.handleAll(
            diff(NODE, -1, 1, 1),
            diff(WAY, 1, 1, 2),
            diff(WAY, 2, 2, 2),
            diff(WAY, 3, 3, 2)
        )

        val updatedElements = handler.getElementUpdates(elements).updated
        assertEquals(4, updatedElements.size)
        val updatedWays = updatedElements.filterIsInstance<Way>()
        assertEquals(3, updatedWays.size)
        assertEquals(listOf(3L,2L,1L), updatedWays.find { it.id == 1L }!!.nodeIds)
        assertEquals(listOf(1L,2L,1L,1L), updatedWays.find { it.id == 2L }!!.nodeIds)
        assertEquals(listOf(3L,4L), updatedWays.find { it.id == 3L }!!.nodeIds)
    }

    @Test fun `updates node id and all relations containing this id`() {
        val elements = listOf<Element>(
            node(-1),
            relation(1, mutableListOf(member(NODE, 3), member(NODE, -1))), // contains it once
            relation(2, mutableListOf(member(NODE, -1), member(NODE, 2), member(NODE, -1))), // contains it multiple times
            relation(3, mutableListOf(member(WAY, -1), member(RELATION, -1), member(NODE, 1))) // contains it not
        )
        val handler = UpdateElementsHandler()
        handler.handle(diff(NODE, -1, 1, 1))
        handler.handleAll(
            diff(NODE, -1, 1, 1),
            diff(RELATION, 1, 1, 2),
            diff(RELATION, 2, 2, 2),
            diff(RELATION, 3, 3, 2)
        )

        val updatedElements = handler.getElementUpdates(elements).updated
        assertEquals(4, updatedElements.size)
        val updatedRelations = updatedElements.filterIsInstance<Relation>()
        assertEquals(3, updatedRelations.size)
        assertEquals(
            listOf(member(NODE, 3), member(NODE, 1)),
            updatedRelations.find { it.id == 1L }!!.members
        )
        assertEquals(
            listOf(member(NODE, 1), member(NODE, 2), member(NODE, 1)),
            updatedRelations.find { it.id == 2L }!!.members
        )
        assertEquals(
            listOf(member(WAY, -1), member(RELATION, -1), member(NODE, 1)),
            updatedRelations.find { it.id == 3L }!!.members
        )
    }

    @Test fun `deletes node id and updates all ways containing this id`() {
        val elements = listOf<Element>(
            node(1),
            way(1, mutableListOf(3,1)), // contains it once
            way(2, mutableListOf(1,2,1)), // contains it multiple times
            way(3, mutableListOf(3,4)) // contains it not
        )
        val handler = UpdateElementsHandler()
        handler.handleAll(
            diff(NODE, 1),
            diff(WAY, 1, 1, 2),
            diff(WAY, 2, 2, 2),
            diff(WAY, 3, 3, 2)
        )

        val elementUpdates = handler.getElementUpdates(elements)
        assertEquals(1, elementUpdates.deleted.size)
        assertEquals(3, elementUpdates.updated.size)
        val updatedWays = elementUpdates.updated.filterIsInstance<Way>()
        assertEquals(3, updatedWays.size)
        assertEquals(listOf(3L), updatedWays.find { it.id == 1L }!!.nodeIds)
        assertEquals(listOf(2L), updatedWays.find { it.id == 2L }!!.nodeIds)
        assertEquals(listOf(3L, 4L), updatedWays.find { it.id == 3L }!!.nodeIds)
    }

    @Test fun `deletes node id and updates all relations containing this id`() {
        val elements = listOf<Element>(
            node(1),
            relation(1, mutableListOf(member(NODE, 3), member(NODE, 1))), // contains it once
            relation(2, mutableListOf(member(NODE, 1), member(NODE, 2), member(NODE, 1))), // contains it multiple times
            relation(3, mutableListOf(member(WAY, 1), member(RELATION, 1), member(NODE, 2))) // contains it not
        )
        val handler = UpdateElementsHandler()
        handler.handleAll(
            diff(NODE, 1),
            diff(RELATION, 1, 1, 2),
            diff(RELATION, 2, 2, 2),
            diff(RELATION, 3, 3, 2)
        )

        val elementUpdates = handler.getElementUpdates(elements)
        assertEquals(1, elementUpdates.deleted.size)
        assertEquals(3, elementUpdates.updated.size)
        val updatedRelations = elementUpdates.updated.filterIsInstance<Relation>()
        assertEquals(3, updatedRelations.size)
        assertEquals(
            listOf(member(NODE, 3)),
            updatedRelations.find { it.id == 1L }!!.members
        )
        assertEquals(
            listOf(member(NODE, 2)),
            updatedRelations.find { it.id == 2L }!!.members
        )
        assertEquals(
            listOf(member(WAY, 1), member(RELATION, 1), member(NODE, 2)),
            updatedRelations.find { it.id == 3L }!!.members
        )
    }
}

private fun node(id: Long) = OsmNode(id, 1, 0.0, 0.0, null)
private fun way(id: Long, nodes: MutableList<Long>) = OsmWay(id, 1, nodes, null)
private fun relation(id: Long, members: MutableList<RelationMember>) = OsmRelation(id, 1, members, null)
private fun member(type: Element.Type, ref: Long) = OsmRelationMember(ref, "", type)

private fun diff(type: Element.Type, oldId: Long, newId: Long? = null, newVersion: Int? = null) =
    DiffElement().also {
        it.type = type
        it.clientId = oldId
        it.serverId = newId
        it.serverVersion = newVersion
    }

private fun UpdateElementsHandler.handleAll(vararg diffs: DiffElement) {
    diffs.forEach { handle(it) }
}
