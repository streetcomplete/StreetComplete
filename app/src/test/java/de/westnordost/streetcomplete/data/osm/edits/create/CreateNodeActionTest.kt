package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class CreateNodeActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test fun `add node`() {
        on(provider.nextNodeId()).thenReturn(-123)

        val tags = mapOf("amenity" to "atm")
        val position = LatLon(12.0, 34.0)
        val dummyNode = node(0, position, tags, 1)
        val action = CreateNodeAction(position, tags)

        val data = action.createUpdates(dummyNode, null, repos, provider)

        assertTrue(data.deletions.isEmpty())
        assertTrue(data.modifications.isEmpty())

        val createdNode = data.creations.single() as Node
        assertEquals(-123, createdNode.id)
        assertEquals(1, createdNode.version)
        assertEquals(tags, createdNode.tags)
        assertEquals(position, createdNode.position)
        assertNotEquals(0, createdNode.timestampEdited)
    }
}
