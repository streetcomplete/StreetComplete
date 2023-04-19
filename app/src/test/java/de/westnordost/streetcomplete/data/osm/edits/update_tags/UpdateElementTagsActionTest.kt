package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
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
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            node(1, p(0.0, 0.0)),
            node(1, p(0.1, 0.0)),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if way was extended or shortened at start`() {
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            way(1, listOf(0, 1, 2, 3)),
            way(1, listOf(1, 2, 3)),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if way was extended or shortened at end`() {
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            way(1, listOf(0, 1, 2, 3)),
            way(1, listOf(0, 1, 2)),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if relation members were removed`() {
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            rel(1, listOf(member(NODE, 1), member(NODE, 2))),
            rel(1, listOf(member(NODE, 1))),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if relation members were added`() {
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            rel(1, listOf(member(NODE, 1))),
            rel(1, listOf(member(NODE, 1), member(NODE, 2))),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if order of relation members changed`() {
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            rel(1, listOf(member(NODE, 2), member(NODE, 1))),
            rel(1, listOf(member(NODE, 1), member(NODE, 2))),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if role of any relation member changed`() {
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(
            rel(1, listOf(member(NODE, 1, "b"))),
            rel(1, listOf(member(NODE, 1, "a"))),
            repos,
            provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `conflict if changes are not applicable`() {
        val w = way(1, listOf(1, 2, 3), mutableMapOf("highway" to "residential"))
        UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street")))
        ).createUpdates(w, w, repos, provider)
    }

    @Test fun `apply changes`() {
        val w = way(1, listOf(1, 2, 3))
        val data = UpdateElementTagsAction(
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street")))
        ).createUpdates(w, w, repos, provider)
        val updatedWay = data.modifications.single() as Way
        assertEquals(mapOf("highway" to "living_street"), updatedWay.tags)
    }
}
