package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.*
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
        on(repos.get(NODE, 1)).thenReturn(node(1, p(1.0, 0.0)))
        UpdateElementTagsAction(
            node(1, p(0.0, 0.0)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if way was extended or shortened at start`() {
        on(repos.get(WAY, 1)).thenReturn(way(1, listOf(1, 2, 3)))
        UpdateElementTagsAction(
            way(1, listOf(0, 1, 2, 3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if way was extended or shortened at end`() {
        on(repos.get(WAY, 1)).thenReturn(way(1, listOf(0, 1, 2)))
        UpdateElementTagsAction(
            way(1, listOf(0, 1, 2, 3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if relation members were removed`() {
        on(repos.get(RELATION, 1)).thenReturn(rel(1, listOf(member(NODE, 1))))
        UpdateElementTagsAction(
            rel(1, listOf(member(NODE, 1), member(NODE, 2))),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if relation members were added`() {
        on(repos.get(RELATION, 1)).thenReturn(rel(1, listOf(member(NODE, 1), member(NODE, 2))))
        UpdateElementTagsAction(
            rel(1, listOf(member(NODE, 1))),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if order of relation members changed`() {
        on(repos.get(RELATION, 1)).thenReturn(rel(1, listOf(member(NODE, 1), member(NODE, 2))))
        UpdateElementTagsAction(
            rel(1, listOf(member(NODE, 2), member(NODE, 1))),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        ).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if role of any relation member changed`() {
        on(repos.get(RELATION, 1)).thenReturn(rel(1, listOf(member(NODE, 1, "a"))))
        UpdateElementTagsAction(
            rel(1, listOf(member(NODE, 1, "b"))),
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
}
