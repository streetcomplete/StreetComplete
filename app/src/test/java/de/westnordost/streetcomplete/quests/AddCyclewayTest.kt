package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.ktx.toEpochMilli
import de.westnordost.streetcomplete.quests.cycleway.*
import de.westnordost.streetcomplete.quests.cycleway.Cycleway.*
import de.westnordost.streetcomplete.util.translate
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class AddCyclewayTest {

    private val questType = AddCycleway()

    @Test fun `applicable to road with missing cycleway`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary"
            ))
        ))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road with nearby cycleway`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2), mapOf(
                "highway" to "primary",
                "width" to "18"
            )),
            way(2L, listOf(3,4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(14.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to road with nearby cycleway that is not aligned to the road`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2), mapOf(
                "highway" to "primary",
                "width" to "18"
            )),
            way(2L, listOf(3,4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(14.0, 135.0)
        val p4 = p3.translate(50.0, 75.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to road with cycleway that is far away enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2), mapOf(
                "highway" to "primary",
                "width" to "18"
            )),
            way(2L, listOf(3,4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(16.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to small road with cycleway that is far away enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2), mapOf(
                "highway" to "primary",
                "lanes" to "2"
            )),
            way(2L, listOf(3,4), mapOf(
                "highway" to "cycleway"
            ))
        ))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(10.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road with cycleway=separate`() {
        assertTrue(questType.getApplicableElements(TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway" to "separate"
            ))
        ))).toList().isEmpty())
        assertTrue(questType.getApplicableElements(TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway:left" to "separate"
            ))
        ))).toList().isEmpty())
        assertTrue(questType.getApplicableElements(TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway:right" to "separate"
            ))
        ))).toList().isEmpty())
        assertTrue(questType.getApplicableElements(TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway:both" to "separate"
            ))
        ))).toList().isEmpty())
    }

    @Test fun `not applicable to road with cycleway that is not old enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway" to "track"
            ), timestamp = Instant.now().toEpochMilli())
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to road with cycleway that is old enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway" to "track",
                "check_date:cycleway" to "2001-01-01"
            ), timestamp = Instant.now().toEpochMilli())
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road with cycleway that is old enough but has unknown cycleway tagging`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1L, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "cycleway" to "whatsthis",
                "check_date:cycleway" to "2001-01-01"
            ), timestamp = Instant.now().toEpochMilli())
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `apply cycleway lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(EXCLUSIVE_LANE),
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:lane", "exclusive")
        )
    }

    @Test fun `apply advisory lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(ADVISORY_LANE),
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:lane", "advisory")
        )
    }

    @Test fun `apply cycleway track answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(TRACK),
            StringMapEntryAdd("cycleway:both", "track")
        )
    }

    @Test fun `apply unspecified cycle lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(UNSPECIFIED_LANE),
            StringMapEntryAdd("cycleway:both", "lane")
        )
    }

    @Test fun `apply bus lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(BUSWAY),
            StringMapEntryAdd("cycleway:both", "share_busway")
        )
    }

    @Test fun `apply pictogram lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(PICTOGRAMS),
            StringMapEntryAdd("cycleway:both", "shared_lane"),
            StringMapEntryAdd("cycleway:both:lane", "pictogram")
        )
    }

    @Test fun `apply suggestion lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(PICTOGRAMS),
            StringMapEntryAdd("cycleway:both", "shared_lane"),
            StringMapEntryAdd("cycleway:both:lane", "pictogram")
        )
    }

    @Test fun `apply no cycleway answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(NONE),
            StringMapEntryAdd("cycleway:both", "no")
        )
    }

    @Test fun `apply cycleway on sidewalk answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(SIDEWALK_EXPLICIT),
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("cycleway:both:segregated", "no")
        )
    }

    @Test fun `apply cycleway on sidewalk answer only on one side`() {
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(SIDEWALK_EXPLICIT), CyclewaySide(NONE)),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:right", "no"),
            // not this: StringMapEntryAdd("sidewalk", "both"),
            StringMapEntryAdd("cycleway:left:segregated", "no")
        )
    }

    private fun bothSidesAnswer(bothSides: Cycleway): CyclewayAnswer {
        val side = CyclewaySide(bothSides)
        return CyclewayAnswer(side, side)
    }

    @Test fun `apply separate cycleway answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(SEPARATE),
            StringMapEntryAdd("cycleway:both", "separate")
        )
    }

    @Test fun `apply dual cycle track answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(DUAL_TRACK),
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryAdd("cycleway:both:oneway", "no")
        )
    }

    @Test fun `apply dual cycle lane answer`() {
        questType.verifyAnswer(
            bothSidesAnswer(DUAL_LANE),
            StringMapEntryAdd("cycleway:both", "lane"),
            StringMapEntryAdd("cycleway:both:oneway", "no"),
            StringMapEntryAdd("cycleway:both:lane", "exclusive")
        )
    }

    @Test fun `apply answer where left and right side are different`() {
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(TRACK), CyclewaySide(NONE)),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:right", "no")
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of oneway`() {
        // this would be a street that has tracks on both sides but is oneway=yes (in countries with
        // right hand traffic)
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(TRACK, -1), CyclewaySide(TRACK), true),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:left:oneway", "-1"),
            StringMapEntryAdd("cycleway:right", "track"),
            StringMapEntryAdd("oneway:bicycle", "no")
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of backward oneway`() {
        // this would be a street that has lanes on both sides but is oneway=-1 (in countries with
        // right hand traffic)
        questType.verifyAnswer(
            CyclewayAnswer(CyclewaySide(TRACK, +1), CyclewaySide(TRACK), true),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:left:oneway", "yes"),
            StringMapEntryAdd("cycleway:right", "track"),
            StringMapEntryAdd("oneway:bicycle", "no")
        )
    }

    @Test fun `apply answer for both deletes any previous answers given for left, right, general`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory",
                "cycleway:left:segregated" to "maybe",
                "cycleway:left:oneway" to "yes",
                "sidewalk:left:bicycle" to "yes",
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "pictogram",
                "cycleway:right:segregated" to "definitely",
                "cycleway:right:oneway" to "yes",
                "sidewalk:right:bicycle" to "yes",
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "cycleway:segregated" to "definitely",
                "cycleway:oneway" to "yes",
                "sidewalk:bicycle" to "yes"
            ),
            bothSidesAnswer(TRACK),
            StringMapEntryAdd("cycleway:both", "track"),
            StringMapEntryDelete("cycleway:left", "lane"),
            StringMapEntryDelete("cycleway:left:lane", "advisory"),
            StringMapEntryDelete("cycleway:left:segregated", "maybe"),
            StringMapEntryDelete("cycleway:left:oneway", "yes"),
            StringMapEntryDelete("sidewalk:left:bicycle", "yes"),
            StringMapEntryDelete("cycleway:right", "shared_lane"),
            StringMapEntryDelete("cycleway:right:lane", "pictogram"),
            StringMapEntryDelete("cycleway:right:segregated", "definitely"),
            StringMapEntryDelete("cycleway:right:oneway", "yes"),
            StringMapEntryDelete("sidewalk:right:bicycle", "yes"),
            StringMapEntryDelete("cycleway", "shared_lane"),
            StringMapEntryDelete("cycleway:lane", "pictogram"),
            StringMapEntryDelete("cycleway:segregated", "definitely"),
            StringMapEntryDelete("cycleway:oneway", "yes"),
            StringMapEntryDelete("sidewalk:bicycle", "yes")
        )
    }

    @Test fun `apply answer for left, right deletes any previous answers given for both, general`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram",
                "cycleway:both:segregated" to "definitely",
                "cycleway:both:oneway" to "yes",
                "sidewalk:both:bicycle" to "yes",
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "cycleway:segregated" to "definitely",
                "cycleway:oneway" to "yes",
                "sidewalk:bicycle" to "yes"
            ),
            CyclewayAnswer(CyclewaySide(TRACK), CyclewaySide(NONE), false),
            StringMapEntryAdd("cycleway:left", "track"),
            StringMapEntryAdd("cycleway:right", "no"),
            StringMapEntryDelete("cycleway:both", "shared_lane"),
            StringMapEntryDelete("cycleway:both:lane", "pictogram"),
            StringMapEntryDelete("cycleway:both:segregated", "definitely"),
            StringMapEntryDelete("cycleway:both:oneway", "yes"),
            StringMapEntryDelete("sidewalk:both:bicycle", "yes"),
            StringMapEntryDelete("cycleway", "shared_lane"),
            StringMapEntryDelete("cycleway:lane", "pictogram"),
            StringMapEntryDelete("cycleway:segregated", "definitely"),
            StringMapEntryDelete("cycleway:oneway", "yes"),
            StringMapEntryDelete("sidewalk:bicycle", "yes")
        )
    }

    @Test fun `deletes lane subkey when new answer is not a lane`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive"
            ),
            bothSidesAnswer(TRACK),
            StringMapEntryModify("cycleway:both", "lane","track"),
            StringMapEntryDelete("cycleway:both:lane", "exclusive")
        )
    }

    @Test fun `deletes shared lane subkey when new answer is not a lane`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            ),
            bothSidesAnswer(TRACK),
            StringMapEntryModify("cycleway:both", "shared_lane","track"),
            StringMapEntryDelete("cycleway:both:lane", "pictogram")
        )
    }

    @Test fun `deletes dual lane tag when new answer is not a dual lane`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            ),
            bothSidesAnswer(EXCLUSIVE_LANE),
            StringMapEntryModify("cycleway:both","lane", "lane"),
            StringMapEntryModify("cycleway:both:lane","exclusive", "exclusive"),
            StringMapEntryDelete("cycleway:both:oneway", "no")
        )
    }

    @Test fun `modifies lane subkey when new answer is different lane`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            ),
            bothSidesAnswer(SUGGESTION_LANE),
            StringMapEntryModify("cycleway:both", "shared_lane","shared_lane"),
            StringMapEntryModify("cycleway:both:lane", "pictogram","advisory")
        )
    }

    @Test fun `deletes dual track tag when new answer is not a dual track`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            ),
            bothSidesAnswer(TRACK),
            StringMapEntryModify("cycleway:both","track", "track"),
            StringMapEntryDelete("cycleway:both:oneway", "no")
        )
    }

    @Test fun `modify segregated tag if new answer is now segregated`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            ),
            bothSidesAnswer(TRACK),
            StringMapEntryModify("cycleway:both","track", "track"),
            StringMapEntryModify("cycleway:both:segregated", "no", "yes")
        )
    }

    @Test fun `modify segregated tag if new answer is now not segregated`() {
        questType.verifyAnswer(
            mapOf(
                "sidewalk" to "both",
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "yes"
            ),
            bothSidesAnswer(SIDEWALK_EXPLICIT),
            StringMapEntryModify("cycleway:both","track", "track"),
            StringMapEntryModify("sidewalk","both", "both"),
            StringMapEntryModify("cycleway:both:segregated", "yes", "no")
        )
    }

    @Test fun `delete segregated tag if new answer is not a track or on sidewalk`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            ),
            bothSidesAnswer(BUSWAY),
            StringMapEntryModify("cycleway:both","track", "share_busway"),
            StringMapEntryDelete("cycleway:both:segregated", "no")
        )
    }

    @Test fun `sets check date if nothing changed`() {
        questType.verifyAnswer(
            mapOf("cycleway:both" to "track"),
            bothSidesAnswer(TRACK),
            StringMapEntryModify("cycleway:both","track", "track"),
            StringMapEntryAdd("check_date:cycleway", LocalDate.now().toCheckDateString())
        )
    }

    @Test fun `updates check date if nothing changed`() {
        questType.verifyAnswer(
            mapOf("cycleway:both" to "track", "check_date:cycleway" to "2000-11-11"),
            bothSidesAnswer(TRACK),
            StringMapEntryModify("cycleway:both","track", "track"),
            StringMapEntryModify("check_date:cycleway", "2000-11-11", LocalDate.now().toCheckDateString())
        )
    }

    @Test fun `remove oneway bicycle no tag if road is also a oneway for bicycles now`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            ),
            bothSidesAnswer(NONE),
            StringMapEntryModify("cycleway:both","no", "no"),
            StringMapEntryDelete("oneway:bicycle", "no")
        )
    }

    @Test fun `isApplicableTo returns null for untagged ways`() {
        assertNull(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified"
            ))
        ))
    }

    @Test fun `isApplicableTo returns true for tagged old ways`() {
        assertTrue(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway" to "track"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertTrue(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:left" to "track"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertTrue(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:right" to "track"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertTrue(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:both" to "track"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
    }

    @Test fun `isApplicableTo returns false for ways having nothing to do with cycleways`() {
        assertFalse(questType.isApplicableTo(way(tags = mapOf("waterway" to "river")))!!)
    }

    @Test fun `isApplicableTo returns false for tagged new ways`() {
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway" to "track"
            ), timestamp = Instant.now().toEpochMilli())
        )!!)
    }

    @Test fun `isApplicableTo returns false for tagged old ways with unknown cycleway values`() {
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:left" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:right" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:both" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
    }

    @Test fun `isApplicableTo returns false for tagged old ways with unknown cycleway=separate values`() {
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway" to "separate"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:left" to "separate"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:right" to "separate"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:both" to "separate"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
    }

    @Test fun `isApplicableTo returns false for tagged old ways with unknown cycleway lane values`() {

        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway" to "lane",
                "cycleway:lane" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
        assertFalse(questType.isApplicableTo(
            way(tags = mapOf(
                "highway" to "unclassified",
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "something"
            ), timestamp = "2000-10-10".toCheckDate()?.toEpochMilli())
        )!!)
    }
}
