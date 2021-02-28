package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementConflictException
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
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

    @Test(expected = ElementConflictException::class)
    fun `conflict if node moved too much`() {
        val p1 = OsmLatLon(0.0, 0.0)
        val p2 = OsmLatLon(0.1,0.0)
        val n = OsmNode(1L, 2, p1, null)
        UpdateElementTagsAction(
            SpatialPartsOfNode(p2),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(n, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if way was extended or shortened at start`() {
        val w = OsmWay(1L, 1, listOf(1,2,3), null)
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(0,1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(w, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if way was extended or shortened at end`() {
        val w = OsmWay(1L, 1, listOf(0,1,2), null)
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(0,1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(w, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if relation members were removed`() {
        val r = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "a", Element.Type.NODE)
        ), null)
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                OsmRelationMember(1L, "a", Element.Type.NODE),
                OsmRelationMember(2L, "a", Element.Type.NODE)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if relation members were added`() {
        val r = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "a", Element.Type.NODE),
            OsmRelationMember(2L, "a", Element.Type.NODE)
        ), null)
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                OsmRelationMember(1L, "a", Element.Type.NODE)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if order of relation members changed`() {
        val r = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "a", Element.Type.NODE),
            OsmRelationMember(2L, "a", Element.Type.NODE)
        ), null)
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                OsmRelationMember(2L, "a", Element.Type.NODE),
                OsmRelationMember(1L, "a", Element.Type.NODE)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if role of any relation member changed`() {
        val r = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "a", Element.Type.NODE)
        ), null)
        UpdateElementTagsAction(
            SpatialPartsOfRelation(arrayListOf(
                OsmRelationMember(1L, "b", Element.Type.NODE)
            )),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }


    @Test(expected = ElementConflictException::class)
    fun `conflict if quest type is not applicable to element`() {
        on(questType.isApplicableTo(any())).thenReturn(false)

        val r = OsmWay(1L, 1, listOf(1,2,3), null)
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test(expected = ElementConflictException::class)
    fun `conflict if changes are not applicable`() {
        val r = OsmWay(1L, 1, listOf(1,2,3), mutableMapOf("highway" to "residential"))
        UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street"))),
            questType
        ).createUpdates(r, repos, provider)
    }

    @Test fun `apply changes`() {
        val r = OsmWay(1L, 1, listOf(1,2,3), null)
        val action = UpdateElementTagsAction(
            SpatialPartsOfWay(arrayListOf(1,2,3)),
            StringMapChanges(listOf(StringMapEntryAdd("highway", "living_street"))),
            questType
        )
        val data = action.createUpdates(r, repos, provider)
        val updatedWay = data.single() as Way
        assertEquals(mapOf("highway" to "living_street"), updatedWay.tags)
    }
}
