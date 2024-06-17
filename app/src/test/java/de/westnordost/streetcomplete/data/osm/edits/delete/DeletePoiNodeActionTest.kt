package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.elementIdProvider
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeletePoiNodeActionTest {

    private val e = node(1, tags = mutableMapOf("amenity" to "atm"), version = 2)

    @Mock
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock(classOf<MapDataRepository>())
        provider = elementIdProvider()
    }

    @Test fun `delete free-floating node`() {
        every { repos.getWaysForNode(1L) }.returns(emptyList())
        every { repos.getRelationsForNode(1L) }.returns(emptyList())
        every { repos.getNode(e.id) }.returns(e)
        val data = DeletePoiNodeAction(e).createUpdates(repos, provider)
        assertTrue(data.modifications.isEmpty())
        assertTrue(data.creations.isEmpty())
        assertEquals(e, data.deletions.single())
    }

    @Test fun `'delete' vertex`() {
        every { repos.getWaysForNode(1L) }.returns(listOf(way()))
        every { repos.getRelationsForNode(1L) }.returns(emptyList())
        every { repos.getNode(e.id) }.returns(e)
        val data = DeletePoiNodeAction(e).createUpdates(repos, provider)
        assertTrue(data.deletions.isEmpty())
        assertTrue(data.creations.isEmpty())
        assertTrue(data.modifications.single().tags.isEmpty())
    }

    @Test
    fun `moved element creates conflict`() {
        every { repos.getNode(e.id) }.returns(e.copy(position = p(1.0, 1.0)))

        assertFailsWith<ConflictException> {
            DeletePoiNodeAction(e).createUpdates(repos, provider)
        }
    }

    @Test fun idsUpdatesApplied() {
        val node = node(id = -1)
        val action = DeletePoiNodeAction(node)
        val idUpdates = mapOf(ElementKey(ElementType.NODE, -1) to 5L)

        assertEquals(
            DeletePoiNodeAction(node.copy(id = 5)),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(ElementType.NODE, -1)),
            DeletePoiNodeAction(node(id = -1)).elementKeys
        )
    }
}
