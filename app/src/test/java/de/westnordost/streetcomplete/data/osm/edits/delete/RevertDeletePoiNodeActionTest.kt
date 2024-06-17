package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.util.ktx.copy
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RevertDeletePoiNodeActionTest {

    private val e = node(1, tags = mutableMapOf("amenity" to "atm"), version = 2)

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test fun `restore deleted element`() {
        assertEquals(
            e.copy(
                version = 3,
                timestampEdited = 0
            ),
            RevertDeletePoiNodeAction(e).createUpdates(repos, provider).modifications
                .single()
                .copy(timestampEdited = 0)
        )
    }

    @Test fun `restore element with cleared tags`() {
        on(repos.getNode(1)).thenReturn(e.copy(version = 3))
        assertEquals(
            e.copy(
                version = 3,
                timestampEdited = 0
            ),
            RevertDeletePoiNodeAction(e).createUpdates(repos, provider).modifications
                .single()
                .copy(timestampEdited = 0)
        )
    }

    @Test
    fun `conflict if there is already a newer version`() {
        on(repos.getNode(1)).thenReturn(e.copy(version = 4))

        assertFailsWith<ConflictException> {
            // version 3 would be the deletion
            RevertDeletePoiNodeAction(e).createUpdates(repos, provider)
        }
    }

    @Test fun idsUpdatesApplied() {
        val node = node(id = -1)
        val action = RevertDeletePoiNodeAction(node)
        val idUpdates = mapOf(ElementKey(ElementType.NODE, -1) to 5L)

        assertEquals(
            RevertDeletePoiNodeAction(node.copy(id = 5)),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(ElementType.NODE, -1)),
            RevertDeletePoiNodeAction(node(id = -1)).elementKeys
        )
    }
}
