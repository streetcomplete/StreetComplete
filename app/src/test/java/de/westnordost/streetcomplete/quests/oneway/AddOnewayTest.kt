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

    @Test fun `applies to road with just one lane`() {
        assertEquals(true, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "lanes" to "1"
        ))))
    }

    @Test fun `applies to slim road`() {
        assertEquals(true, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "4"
        ))))
    }

    @Test fun `does not apply to wide road`() {
        assertEquals(false, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "5"
        ))))
    }

    @Test fun `applies to wider road that has parking lanes`() {
        assertEquals(true, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "12",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        ))))
    }

    @Test fun `does not apply to wider road that has parking lanes but not enough`() {
        assertEquals(false, questType.isApplicableTo(createWay(mapOf(
            "highway" to "residential",
            "width" to "13",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        ))))
    }
}

private fun createWay(tags: Map<String, String>?) = OsmWay(1,1, listOf(1,2,3), tags)