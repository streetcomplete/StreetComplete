package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.changesApplied
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class CreateNodeFromVertexActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test
    fun `conflict when node position changed`() {
        val n = node()
        val n2 = n.copy(position = n.position.translate(1.0, 0.0)) // moved by 1 meter
        on(repos.getNode(n.id)).thenReturn(n2)
        on(repos.getWaysForNode(n.id)).thenReturn(listOf())

        assertFailsWith<ConflictException> {
            CreateNodeFromVertexAction(n, StringMapChanges(listOf()), listOf())
                .createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict when node is not part of exactly the same ways as before`() {
        val n = node()
        on(repos.getNode(n.id)).thenReturn(n)
        on(repos.getWaysForNode(n.id)).thenReturn(listOf(way(1), way(2)))

        assertFailsWith<ConflictException> {
            CreateNodeFromVertexAction(n, StringMapChanges(listOf()), listOf(1L))
                .createUpdates(repos, provider)
        }
    }

    @Test
    fun `create updates`() {
        val n = node()
        on(repos.getNode(n.id)).thenReturn(n)
        on(repos.getWaysForNode(n.id)).thenReturn(listOf(way(1), way(2)))

        val changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b")))

        val data = CreateNodeFromVertexAction(n, changes, listOf(1L, 2L)).createUpdates(repos, provider)

        val n2 = n.changesApplied(changes)

        assertEquals(MapDataChanges(modifications = listOf(n2)), data)
    }

    @Test fun idsUpdatesApplied() {
        val node = node(id = -1)
        val action = CreateNodeFromVertexAction(
            node,
            StringMapChanges(listOf()),
            listOf(-1, -2, 3) // and one that doesn't get updated
        )
        val idUpdates = mapOf(
            ElementKey(ElementType.WAY, -1) to 99L,
            ElementKey(ElementType.WAY, -2) to 5L,
            ElementKey(ElementType.NODE, -1) to 999L,
        )

        assertEquals(
            CreateNodeFromVertexAction(
                node.copy(id = 999),
                StringMapChanges(listOf()),
                listOf(99, 5, 3)
            ),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(
                ElementKey(ElementType.WAY, -1),
                ElementKey(ElementType.WAY, -2),
                ElementKey(ElementType.WAY, 3),
                ElementKey(ElementType.NODE, -1),
            ),
            CreateNodeFromVertexAction(
                node(id = -1),
                StringMapChanges(listOf()),
                listOf(-1, -2, 3) // and one that doesn't get updated
            ).elementKeys
        )
    }
}
