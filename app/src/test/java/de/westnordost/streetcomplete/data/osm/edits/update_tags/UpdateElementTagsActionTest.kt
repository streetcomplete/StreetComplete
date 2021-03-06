package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateElementTagsActionTest {

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider
    private lateinit var questType: OsmElementQuestType<*>

    @Before fun setUp() {
        repos = mock()
        provider = mock()
        questType = mock()
        on(questType.isApplicableTo(any())).thenReturn(true)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if node moved too much`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(0.1,0.0)
        val n = node(1, p1)
        UpdateElementTagsAction(
            SpatialPartsOfNode(p2),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(n, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if way was extended or shortened at start`() {
        val w = way(1, listOf(1,2,3))
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(0,1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(w, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if way was extended or shortened at end`() {
        val w = way(1,listOf(0,1,2))
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(0,1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(w, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if relation members were removed`() {
        val r = rel(1, listOf(
            member(Element.Type.NODE, 1)
        ))

        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                member(Element.Type.NODE, 1),
                member(Element.Type.NODE, 2)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if relation members were added`() {
        val r = rel(1, listOf(
            member(Element.Type.NODE, 1),
            member(Element.Type.NODE, 2)
        ))
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                member(Element.Type.NODE, 1)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if order of relation members changed`() {
        val r = rel(1, listOf(
            member(Element.Type.NODE, 1),
            member(Element.Type.NODE, 2)
        ))
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                member(Element.Type.NODE, 2),
                member(Element.Type.NODE, 1)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if role of any relation member changed`() {
        val r = rel(1, listOf(
            member(Element.Type.NODE, 1, "a")
        ))
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                member(Element.Type.NODE, 1, "b")
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }


    @Test(expected = ConflictException::class)
    fun `conflict if quest type is not applicable to element`() {
        on(questType.isApplicableTo(any())).thenReturn(false)

        val w = way(1,listOf(1,2,3))
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(w, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict if changes are not applicable`() {
        val w = way(1, listOf(1,2,3), mutableMapOf("highway" to "residential"))
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street"))),
            questType
        ).createUpdates(w, repos, provider)
    }

    @Test fun `apply changes`() {
        val w = way(1, listOf(1,2,3))
        val action = UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street"))),
            questType
        )
        val data = action.createUpdates(w, repos, provider)
        val updatedWay = data.single() as Way
        assertEquals(mapOf("highway" to "living_street"), updatedWay.tags)
    }
}
