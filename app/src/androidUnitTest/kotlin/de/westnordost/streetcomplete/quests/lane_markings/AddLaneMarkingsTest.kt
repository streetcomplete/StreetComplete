package de.westnordost.streetcomplete.quests.lane_markings

import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddLaneMarkingsTest {
    private val questType = AddLaneMarkings()

    @Test fun `is applicable to residential roads if speed above 33`() {
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:urban",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:urban",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "34",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "30 mph",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
    }

    @Test fun `is not applicable to residential roads if speed is below or equal 33`() {
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "DE:zone30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "source:maxspeed" to "DE:zone:30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed:type" to "DE:30",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "33",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "20 mph",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
        assertFalse(questType.isApplicableTo(way(tags = mapOf(
            "maxspeed" to "walk",
            "highway" to "residential",
            "surface" to "asphalt"
        ))))
    }
}
