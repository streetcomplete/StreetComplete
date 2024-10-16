package de.westnordost.streetcomplete.quests.width

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddRoadWidthTest {
    private val quest = AddRoadWidth(mock())

    @Test fun `is applicable to residential roads if speed below 33`() {
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:20",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "32",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "20 mph",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "walk",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
    }

    @Test fun `is not applicable to residential roads if speed is 33 or more`() {
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:urban",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:urban",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "33",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "30 mph",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "variable",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
    }

    @Test fun `is applicable to road with choker`() {
        assertTrue(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt",
            "traffic_calming" to "choker"
        ))))
    }

    @Test fun `is not applicable to road with choker and maxwidth`() {
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt",
            "traffic_calming" to "choker",
            "maxwidth" to "3"
        ))))
        assertFalse(quest.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt",
            "traffic_calming" to "choker",
            "width" to "3"
        ))))
    }

    @Test fun `apply to street`() {
        assertEquals(
            setOf(StringMapEntryAdd("width", "3")),
            quest.answerApplied(WidthAnswer(LengthInMeters(3.0), false))
        )
    }

    @Test fun `apply to street when measured with AR`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("width", "3"),
                StringMapEntryAdd("source:width", "ARCore")
            ),
            quest.answerApplied(WidthAnswer(LengthInMeters(3.0), true))
        )
    }

    @Test fun `apply to street when not measured with AR but previously was`() {
        assertEquals(
            setOf(
                StringMapEntryModify("width", "2", "3"),
                StringMapEntryDelete("source:width", "estimate")
            ),
            quest.answerAppliedTo(
                WidthAnswer(LengthInMeters(3.0), false),
                mapOf("width" to "2", "source:width" to "estimate")
            )
        )
    }

    @Test fun `modifies width-carriageway if set`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("width", "3"),
                StringMapEntryModify("width:carriageway", "2", "3"),
            ),
            quest.answerAppliedTo(
                WidthAnswer(LengthInMeters(3.0), false),
                mapOf("width:carriageway" to "2")
            )
        )
    }

    @Test fun `apply to choker`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxwidth", "3")),
            quest.answerAppliedTo(
                WidthAnswer(LengthInMeters(3.0), false),
                mapOf("traffic_calming" to "choker")
            )
        )
    }

    @Test fun `apply to choker when measured with AR`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxwidth", "3"),
                StringMapEntryAdd("source:maxwidth", "ARCore")
            ),
            quest.answerAppliedTo(
                WidthAnswer(LengthInMeters(3.0), true),
                mapOf("traffic_calming" to "choker")
            )
        )
    }

    @Test fun `apply to choker when not measured with AR but previously was`() {
        assertEquals(
            setOf(
                StringMapEntryModify("maxwidth", "2", "3"),
                StringMapEntryDelete("source:maxwidth", "estimate")
            ),
            quest.answerAppliedTo(
                WidthAnswer(LengthInMeters(3.0), false),
                mapOf(
                    "traffic_calming" to "choker",
                    "maxwidth" to "2",
                    "source:maxwidth" to "estimate"
                )
            )
        )
    }
}
