package de.westnordost.streetcomplete.data.osm.edits.move

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.testutils.elementIdProvider
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.util.ktx.copy
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RevertMoveNodeActionTest {

    @Mock
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock(classOf<MapDataRepository>())
        provider = elementIdProvider()
    }

    @Test fun unmoveIt() {
        val n = node()
        val p = p(0.0, 1.0)
        val movedNode = n.copy(position = p)
        every { repos.getNode(n.id) }.returns(movedNode)
        val updates = RevertMoveNodeAction(n).createUpdates(repos, provider)
        assertTrue(updates.creations.isEmpty())
        assertTrue(updates.deletions.isEmpty())
        assertEquals(n, updates.modifications.single().copy(timestampEdited = n.timestampEdited))
    }

    @Test fun idsUpdatesApplied() {
        val node = node(id = -1)
        val action = RevertMoveNodeAction(node)
        val idUpdates = mapOf(ElementKey(ElementType.NODE, -1) to 5L)

        assertEquals(
            RevertMoveNodeAction(node.copy(id = 5)),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(ElementType.NODE, -1)),
            RevertMoveNodeAction(node(id = -1)).elementKeys
        )
    }
}
