package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.mapdata.DiffElement
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.UpdatedElementsHandler
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatedElementsHandlerTest {
    @Test fun `updates element version`() {
        val handler = UpdatedElementsHandler()
        handler.handle(DiffElement(NODE, 1, 1, 123))

        val element = handler.getElementUpdates(listOf(node(1))).updated.single()
        assertEquals(123, element.version)
    }

    @Test fun `deletes element`() {
        val handler = UpdatedElementsHandler()
        handler.handle(DiffElement(NODE, 1))

        val deletedElementKey = handler.getElementUpdates(listOf(node(1))).deleted.single()
        assertEquals(1, deletedElementKey.id)
        assertEquals(NODE, deletedElementKey.type)
    }

    @Test fun `updates element id`() {
        val handler = UpdatedElementsHandler()
        handler.handle(DiffElement(NODE, -1, 123456, 1))

        val element = handler.getElementUpdates(listOf(node(-1))).updated.single()
        assertEquals(123456, element.id)
    }

    @Test fun `updates node id and all ways containing this id`() {
        val elements = listOf(
            node(-1),
            way(1, listOf(3, 2, -1)), // contains it once
            way(2, listOf(-1, 2, -1, -1)), // contains it multiple times
            way(3, listOf(3, 4)) // contains it not
        )
        val handler = UpdatedElementsHandler()
        handler.handleAll(
            DiffElement(NODE, -1, 1, 1),
            DiffElement(WAY, 1, 1, 2),
            DiffElement(WAY, 2, 2, 2),
            DiffElement(WAY, 3, 3, 2)
        )

        val updatedElements = handler.getElementUpdates(elements).updated
        assertEquals(4, updatedElements.size)
        val updatedWays = updatedElements.filterIsInstance<Way>()
        assertEquals(3, updatedWays.size)
        assertEquals(listOf(3L, 2L, 1L), updatedWays.find { it.id == 1L }!!.nodeIds)
        assertEquals(listOf(1L, 2L, 1L, 1L), updatedWays.find { it.id == 2L }!!.nodeIds)
        assertEquals(listOf(3L, 4L), updatedWays.find { it.id == 3L }!!.nodeIds)
    }

    @Test fun `updates node id and all relations containing this id`() {
        val elements = listOf(
            node(-1),
            rel(1, listOf(member(NODE, 3), member(NODE, -1))), // contains it once
            rel(2, listOf(member(NODE, -1), member(NODE, 2), member(NODE, -1))), // contains it multiple times
            rel(3, listOf(member(WAY, -1), member(RELATION, -1), member(NODE, 1))) // contains it not
        )
        val handler = UpdatedElementsHandler()
        handler.handle(DiffElement(NODE, -1, 1, 1))
        handler.handleAll(
            DiffElement(NODE, -1, 1, 1),
            DiffElement(RELATION, 1, 1, 2),
            DiffElement(RELATION, 2, 2, 2),
            DiffElement(RELATION, 3, 3, 2)
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
        val elements = listOf(
            node(1),
            way(1, listOf(3, 1)), // contains it once
            way(2, listOf(1, 2, 1)), // contains it multiple times
            way(3, listOf(3, 4)) // contains it not
        )
        val handler = UpdatedElementsHandler()
        handler.handleAll(
            DiffElement(NODE, 1),
            DiffElement(WAY, 1, 1, 2),
            DiffElement(WAY, 2, 2, 2),
            DiffElement(WAY, 3, 3, 2)
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
        val elements = listOf(
            node(1),
            rel(1, listOf(member(NODE, 3), member(NODE, 1))), // contains it once
            rel(2, listOf(member(NODE, 1), member(NODE, 2), member(NODE, 1))), // contains it multiple times
            rel(3, listOf(member(WAY, 1), member(RELATION, 1), member(NODE, 2))) // contains it not
        )
        val handler = UpdatedElementsHandler()
        handler.handleAll(
            DiffElement(NODE, 1),
            DiffElement(RELATION, 1, 1, 2),
            DiffElement(RELATION, 2, 2, 2),
            DiffElement(RELATION, 3, 3, 2)
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

    @Test fun `relays element id updates of non-deleted elements`() {
        val elements = listOf(
            node(-1),
            node(-2),
            way(-3, listOf()),
            rel(-4, listOf())
        )
        val handler = UpdatedElementsHandler()
        handler.handleAll(
            DiffElement(NODE, -1, 11),
            DiffElement(NODE, -2, null),
            DiffElement(WAY, -3, 33),
            DiffElement(RELATION, -4, 44)
        )
        val updates = handler.getElementUpdates(elements)
        assertTrue(updates.idUpdates.containsExactlyInAnyOrder(listOf(
            ElementIdUpdate(NODE, -1, 11),
            ElementIdUpdate(WAY, -3, 33),
            ElementIdUpdate(RELATION, -4, 44),
        )))
        updates.deleted.containsExactlyInAnyOrder(listOf(
            ElementKey(NODE, -2)
        ))
    }

    @Test fun `does nothing with ignored relation types`() {
        val elements = listOf(rel(-4, listOf(), tags = mapOf("type" to "route")))
        val ignoredRelationTypes = setOf("route")
        val handler = UpdatedElementsHandler(ignoredRelationTypes)
        handler.handle(DiffElement(RELATION, -4, 44))
        val updates = handler.getElementUpdates(elements)
        assertTrue(updates.idUpdates.isEmpty())
    }
}

private fun UpdatedElementsHandler.handleAll(vararg diffs: DiffElement) {
    diffs.forEach { handle(it) }
}
