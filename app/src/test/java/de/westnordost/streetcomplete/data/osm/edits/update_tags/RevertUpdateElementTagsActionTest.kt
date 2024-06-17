package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.elementIdProvider
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RevertUpdateElementTagsActionTest {

    @Mock
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock(classOf<MapDataRepository>())
        provider = elementIdProvider()
    }

    @Test
    fun `conflict if node moved too much`() {
        every { repos.get(ElementType.NODE, 1) }.returns(node(1, p(1.0, 0.0)))

        assertFailsWith<ConflictException> {
            RevertUpdateElementTagsAction(
                node(1, p(0.0, 0.0)),
                StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
            ).createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict if changes are not applicable`() {
        val w = way(1, listOf(1, 2, 3), mutableMapOf("highway" to "residential"))
        every { repos.get(ElementType.WAY, 1) }.returns(w)

        assertFailsWith<ConflictException> {
            RevertUpdateElementTagsAction(
                w,
                StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street")))
            ).createUpdates(repos, provider)
        }
    }

    @Test fun `apply changes`() {
        val w = way(1, listOf(1, 2, 3))
        every { repos.get(ElementType.WAY, 1) }.returns(w)
        val data = RevertUpdateElementTagsAction(
            w,
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street")))
        ).createUpdates(repos, provider)
        val updatedWay = data.modifications.single() as Way
        assertEquals(mapOf("highway" to "living_street"), updatedWay.tags)
    }

    @Test fun idsUpdatesApplied() {
        val way = way(id = -1)
        val action = RevertUpdateElementTagsAction(way, StringMapChanges(listOf()))
        val idUpdates = mapOf(ElementKey(ElementType.WAY, -1) to 5L)

        assertEquals(
            RevertUpdateElementTagsAction(way.copy(id = 5), StringMapChanges(listOf())),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(ElementType.WAY, -1)),
            RevertUpdateElementTagsAction(way(id = -1), StringMapChanges(listOf())).elementKeys
        )
    }
}
