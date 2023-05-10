package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.*
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateElementTagsActionTest {

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test(expected = ConflictException::class)
    fun `conflict if node moved too much`() {
        on(repos.get(NODE, 1)).thenReturn(node(1, p(1.0, 0.0)))
        UpdateElementTagsAction(
            node(1, p(0.0, 0.0)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if changes are not applicable`() {
        val w = way(1, listOf(1, 2, 3), mutableMapOf("highway" to "residential"))
        on(repos.get(WAY, 1)).thenReturn(w)
        UpdateElementTagsAction(
            w,
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street")))
        ).createUpdates(repos, provider)
    }

    @Test fun `apply changes`() {
        val w = way(1, listOf(1, 2, 3))
        on(repos.get(WAY, 1)).thenReturn(w)
        val data = UpdateElementTagsAction(
            w,
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street")))
        ).createUpdates(repos, provider)
        val updatedWay = data.modifications.single() as Way
        assertEquals(mapOf("highway" to "living_street"), updatedWay.tags)
    }

    @Test fun idsUpdatesApplied() {
        val way = way(id = -1)
        val action = UpdateElementTagsAction(way, StringMapChanges(listOf()))
        val idUpdates = mapOf(ElementKey(WAY, -1) to 5L)

        assertEquals(
            UpdateElementTagsAction(way.copy(id = 5), StringMapChanges(listOf())),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(WAY, -1)),
            UpdateElementTagsAction(way(id = -1), StringMapChanges(listOf())).elementKeys
        )
    }
}
