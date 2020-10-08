package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.mock
import org.junit.Assert.*
import org.junit.Test

class AddOnewayTest {
    private val questType = AddOneway(mock())

    @Test fun `does not apply to element without tags`() {
        assertEquals(false, questType.isApplicableTo(createWay(null)))
    }

    @Test fun `applies to slim road`() {
        assertEquals(true, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "1"
        ))))
    }

    @Test fun `does not apply to wide road`() {
        assertEquals(false, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "5",
            "lanes" to "1"
        ))))
    }

    @Test fun `applies to wider road that has parking lanes`() {
        assertEquals(true, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "12",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        ))))
    }

    @Test fun `does not apply to wider road that has parking lanes but not enough`() {
        assertEquals(false, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "13",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        ))))
    }

    @Test fun `does not apply to slim road with more than one lane`() {
        assertEquals(false, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "2"
        ))))
    }
}

private fun createWay(tags: Map<String, String>?) = OsmWay(1,1, listOf(1,2,3), tags)