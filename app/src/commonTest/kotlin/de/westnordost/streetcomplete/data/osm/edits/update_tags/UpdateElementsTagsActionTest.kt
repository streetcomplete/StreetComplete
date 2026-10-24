package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.*
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.testutils.node
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateElementsTagsActionTest {

    private lateinit var repos: MapDataRepository
    private val provider = ElementIdProvider(emptyList())

    @BeforeTest fun setUp() {
        repos = mock()
    }

    @Test fun `apply changes to all elements`() {
        val n1 = node(1)
        val n2 = node(2)
        every { repos.get(NODE, 1) } returns n1
        every { repos.get(NODE, 2) } returns n2

        val data = UpdateElementsTagsAction(listOf(
            UpdateElementTagsAction(n1, StringMapChanges(listOf(StringMapEntryAdd("a", "b")))),
            UpdateElementTagsAction(n2, StringMapChanges(listOf(StringMapEntryAdd("c", "d")))),
        )).createUpdates(repos, provider)

        assertEquals(
            listOf(mapOf("a" to "b"), mapOf("c" to "d")),
            data.modifications.map { it.tags }
        )
    }

    @Test fun `conflict if changes to any element are not applicable`() {
        val n1 = node(1)
        val n2 = node(2, tags = mapOf("c" to "x"))
        every { repos.get(NODE, 1) } returns n1
        every { repos.get(NODE, 2) } returns n2

        assertFailsWith<ConflictException> {
            UpdateElementsTagsAction(listOf(
                UpdateElementTagsAction(n1, StringMapChanges(listOf(StringMapEntryAdd("a", "b")))),
                UpdateElementTagsAction(n2, StringMapChanges(listOf(StringMapEntryAdd("c", "d")))),
            )).createUpdates(repos, provider)
        }
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(NODE, 1), ElementKey(NODE, 2)),
            UpdateElementsTagsAction(listOf(
                UpdateElementTagsAction(node(1), StringMapChanges(listOf(StringMapEntryAdd("a", "b")))),
                UpdateElementTagsAction(node(2), StringMapChanges(listOf(StringMapEntryAdd("c", "d")))),
            )).elementKeys
        )
    }

    @Test fun idsUpdatesApplied() {
        val n = node(id = -1)
        val changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b")))
        val idUpdates = mapOf(ElementKey(NODE, -1) to 5L)

        assertEquals(
            UpdateElementsTagsAction(listOf(UpdateElementTagsAction(n.copy(id = 5), changes))),
            UpdateElementsTagsAction(listOf(UpdateElementTagsAction(n, changes))).idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun createReverted() {
        val action1 = UpdateElementTagsAction(node(1), StringMapChanges(listOf(StringMapEntryAdd("a", "b"))))
        val action2 = UpdateElementTagsAction(node(2), StringMapChanges(listOf(StringMapEntryAdd("c", "d"))))

        assertEquals(
            RevertUpdateElementsTagsAction(listOf(
                action1.createReverted(provider),
                action2.createReverted(provider),
            )),
            UpdateElementsTagsAction(listOf(action1, action2)).createReverted(provider)
        )
    }
}
