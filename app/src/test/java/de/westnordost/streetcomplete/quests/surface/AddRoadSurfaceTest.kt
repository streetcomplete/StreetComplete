package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddRoadSurfaceTest {
    private val questType = AddRoadSurface()

    @Test fun `not applicable to tagged surface`() {
        assertIsNotApplicable("highway" to "residential", "surface" to "asphalt")
    }

    // see https://github.com/streetcomplete/StreetComplete/pull/5453#issuecomment-1911891944
    @Test fun `not applicable to tagged complex surface lanes - paved+note:surface`() {
        assertIsNotApplicable("highway" to "residential", "surface" to "paved", "note:surface" to "(alternative note format) varying asphalt and concrete")
    }
    @Test fun `not applicable to tagged complex surface lanes - paved+surface:lanes`() {
        assertIsNotApplicable("highway" to "residential", "surface" to "paved", "surface:lanes" to "concrete|asphalt|asphalt")
    }
    @Test fun `not applicable to tagged complex surface lanes - unpaved+surface:lanes:forward`() {
        assertIsNotApplicable("highway" to "track", "surface" to "unpaved", "surface:lanes:forward" to "compacted", "surface:lanes:backward" to "gravel" )
    }
    @Test fun `not applicable to tagged complex surface lanes - paved+surface:lanes:both_ways`() {
        assertIsNotApplicable("highway" to "residential", "surface" to "paved", "surface:lanes:both_ways" to "asphalt|concrete")
    }
    @Test fun `applicable to tagged complex surface lanes without surface - surface=null+surface:lanes`() {
        assertIsApplicable("highway" to "residential", "surface:lanes" to "concrete|asphalt|cobblestone")
    }
    @Test fun `applicable to tagged complex surface lanes with asphalt - asphalt+surface:lanes`() {
        assertIsApplicable("highway" to "residential", "surface" to "asphalt", "surface:lanes" to "concrete|asphalt|asphalt")
    }

    @Test fun `applicable to old enough road with surface, regardless of existing surface:lanes or surface:note`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "residential",
            "surface" to "paved",
            "surface:lanes" to "asphalt|concrete",
            "surface:note" to "wildly mixed asphalt, concrete, paving stones and sett",
            "check_date:surface" to "2001-01-01"
        ), timestamp = nowAsEpochMilliseconds())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way))
    }

    @Test fun `applicable to untagged surface`() {
        assertIsApplicable("highway" to "residential")
    }

    @Test fun `applicable where poor tracktype conflicts with paved surface`() {
        assertIsApplicable("highway" to "track", "surface" to "asphalt", "tracktype" to "grade5")
    }

    @Test fun `applicable where high quality tracktype conflicts with poor surface`() {
        assertIsApplicable("highway" to "track", "surface" to "sand", "tracktype" to "grade1")
    }

    @Test fun `not applicable to tagged surface with fitting tracktype`() {
        assertIsNotApplicable("highway" to "track", "surface" to "asphalt", "tracktype" to "grade1")
    }

    @Test fun `applicable to surface tags not providing proper info`() {
        assertIsApplicable("highway" to "residential", "surface" to "paved")
        assertIsNotApplicable("highway" to "residential", "surface" to "paved", "surface:note" to "wildly mixed asphalt, concrete, paving stones and sett")
        assertIsApplicable("highway" to "residential", "surface" to "cobblestone")
        assertIsApplicable("highway" to "residential", "surface" to "cement")
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    @Test fun `not applicable where very poor tracktype and surface match is suspicious, but not conflicting`() {
        assertIsNotApplicable("highway" to "track", "surface" to "gravel", "tracktype" to "grade5")
    }

    @Test fun `not applicable where tracktype and very good surface match is suspicious, but not conflicting`() {
        assertIsNotApplicable("highway" to "track", "surface" to "asphalt", "tracktype" to "grade2")
    }

    @Test fun `not applicable to generic unpaved track with a note and nonconflicting tracktype`() {
        assertIsNotApplicable("highway" to "track", "surface" to "unpaved", "tracktype" to "grade3", "surface:note" to "varying patches with more and less gravel")
    }

}
