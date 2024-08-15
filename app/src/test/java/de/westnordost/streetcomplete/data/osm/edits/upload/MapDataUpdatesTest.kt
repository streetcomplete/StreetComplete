package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.mapdata.DeleteElement
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.UpdateElement
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.createMapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapDataUpdatesTest {
        @Test fun `updates element version`() {
        val updates = createMapDataUpdates(
            elements = listOf(node(1), way(2), rel(3)),
            updates = mapOf(
                ElementKey(NODE, 1) to UpdateElement(1L, 123),
                ElementKey(WAY, 2) to UpdateElement(2L, 124),
                ElementKey(RELATION, 3) to UpdateElement(3L, 125),
            )
        )

        val elements = updates.updated.associateBy { it.key }
        assertEquals(123, elements[ElementKey(NODE, 1)]?.version)
        assertEquals(124, elements[ElementKey(WAY, 2)]?.version)
        assertEquals(125, elements[ElementKey(RELATION, 3)]?.version)
        assertTrue(updates.deleted.isEmpty())
        assertTrue(updates.idUpdates.isEmpty())
    }

    @Test fun `deletes element`() {
        val updates = createMapDataUpdates(
            elements = listOf(node(1), way(2), rel(3)),
            updates = mapOf(
                ElementKey(NODE, 1) to DeleteElement,
                ElementKey(WAY, 2) to DeleteElement,
                ElementKey(RELATION, 3) to DeleteElement,
            )
        )

        assertTrue(updates.updated.isEmpty())
        assertTrue(updates.idUpdates.isEmpty())
        assertEquals(
            setOf(
                ElementKey(NODE, 1),
                ElementKey(WAY, 2),
                ElementKey(RELATION, 3)
            ),
            updates.deleted.toSet()
        )
    }

    @Test fun `updates element id`() {
        val updates = createMapDataUpdates(
            elements = listOf(node(1), way(2), rel(3)),
            updates = mapOf(
                ElementKey(NODE, 1) to UpdateElement(12L, 1),
                ElementKey(WAY, 2) to UpdateElement(22L, 1),
                ElementKey(RELATION, 3) to UpdateElement(32L, 1),
            )
        )

        assertEquals(
            setOf(
                ElementKey(NODE, 12),
                ElementKey(WAY, 22),
                ElementKey(RELATION, 32)
            ),
            updates.updated.mapTo(HashSet()) { it.key }
        )

        assertTrue(updates.deleted.isEmpty())
        assertEquals(
            setOf(
                ElementIdUpdate(NODE, 1, 12),
                ElementIdUpdate(WAY, 2, 22),
                ElementIdUpdate(RELATION, 3, 32)
            ),
            updates.idUpdates.toSet()
        )
    }

    @Test fun `updates node id and all ways containing this id`() {
        val updates = createMapDataUpdates(
            elements = listOf(
                node(-1),
                way(1, listOf(3, 2, -1)), // contains it once
                way(2, listOf(-1, 2, -1, -1)), // contains it multiple times
                way(3, listOf(3, 4)) // contains it not
            ),
            updates = mapOf(ElementKey(NODE, -1) to UpdateElement(1L, 1),)
        )

        val ways = updates.updated.filterIsInstance<Way>().associateBy { it.id }
        assertEquals(2, ways.size)
        assertEquals(listOf<Long>(3, 2, 1), ways[1]?.nodeIds)
        assertEquals(listOf<Long>(1, 2, 1, 1), ways[2]?.nodeIds)

        assertTrue(updates.deleted.isEmpty())
        assertEquals(listOf(ElementIdUpdate(NODE, -1, 1)), updates.idUpdates)
    }

    @Test fun `updates node id and all relations containing this id`() {
        val updates = createMapDataUpdates(
            elements = listOf(
                node(-1),
                rel(1, listOf(member(NODE, 3), member(NODE, -1))), // contains it once
                rel(2, listOf(member(NODE, -1), member(NODE, 2), member(NODE, -1))), // contains it multiple times
                rel(3, listOf(member(WAY, -1), member(RELATION, -1), member(NODE, 1))) // contains it not
            ),
            updates = mapOf(ElementKey(NODE, -1) to UpdateElement(1L, 1),)
        )

        val relations = updates.updated.filterIsInstance<Relation>().associateBy { it.id }
        assertEquals(2, relations.size)
        assertEquals(
            listOf(member(NODE, 3), member(NODE, 1)),
            relations[1]?.members
        )
        assertEquals(
            listOf(member(NODE, 1), member(NODE, 2), member(NODE, 1)),
            relations[2]?.members
        )

        assertTrue(updates.deleted.isEmpty())
        assertEquals(listOf(ElementIdUpdate(NODE, -1, 1)), updates.idUpdates)
    }

    @Test fun `deletes node id and updates all ways containing this id`() {
        val updates = createMapDataUpdates(
            elements = listOf(
                node(1),
                way(1, listOf(3, 1)), // contains it once
                way(2, listOf(1, 2, 1)), // contains it multiple times
                way(3, listOf(3, 4)) // contains it not
            ),
            updates = mapOf(ElementKey(NODE, 1) to DeleteElement)
        )

        assertTrue(updates.idUpdates.isEmpty())
        assertEquals(listOf(ElementKey(NODE, 1)), updates.deleted)

        val ways = updates.updated.filterIsInstance<Way>().associateBy { it.id }
        assertEquals(2, ways.size)
        assertEquals(listOf<Long>(3), ways[1]?.nodeIds)
        assertEquals(listOf<Long>(2), ways[2]?.nodeIds)
    }

    @Test fun `deletes node id and updates all relations containing this id`() {
        val updates = createMapDataUpdates(
            elements = listOf(
                node(1),
                rel(1, listOf(member(NODE, 3), member(NODE, 1))), // contains it once
                rel(2, listOf(member(NODE, 1), member(NODE, 2), member(NODE, 1))), // contains it multiple times
                rel(3, listOf(member(WAY, 1), member(RELATION, 1), member(NODE, 2))) // contains it not
            ),
            updates = mapOf(ElementKey(NODE, 1) to DeleteElement)
        )
        assertTrue(updates.idUpdates.isEmpty())
        assertEquals(listOf(ElementKey(NODE, 1)), updates.deleted)

        val relations = updates.updated.filterIsInstance<Relation>().associateBy { it.id }
        assertEquals(2, relations.size)
        assertEquals(listOf(member(NODE, 3)), relations[1]?.members)
        assertEquals(listOf(member(NODE, 2)), relations[2]?.members)
    }

    @Test fun `does nothing with ignored relation types`() {
        val updates = createMapDataUpdates(
            elements = listOf(
                rel(-4, tags = mapOf("type" to "route"))
            ),
            updates = mapOf(ElementKey(RELATION, -4) to UpdateElement(4, 1)),
            ignoreRelationTypes = setOf("route")
        )
        assertTrue(updates.idUpdates.isEmpty())
        assertTrue(updates.updated.isEmpty())
        assertTrue(updates.deleted.isEmpty())
    }

    @Test fun `references to ignored relation types are updated`() {
        val updates = createMapDataUpdates(
            elements = listOf(
                rel(1, members = listOf(member(RELATION, -4))),
                rel(-4, tags = mapOf("type" to "route"))
            ),
            updates = mapOf(ElementKey(RELATION, -4) to UpdateElement(4, 1)),
            ignoreRelationTypes = setOf("route")
        )
        assertTrue(updates.idUpdates.isEmpty())
        assertEquals(
            listOf(member(RELATION, 4)),
            (updates.updated.single() as Relation).members
        )
        assertTrue(updates.deleted.isEmpty())
    }
}
