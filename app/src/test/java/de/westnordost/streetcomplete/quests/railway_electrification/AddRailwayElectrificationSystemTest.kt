package de.westnordost.streetcomplete.quests.railway_electrification

import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.*
import org.junit.Test

class AddRailwayElectrificationSystemTest {
    private val questType = AddRailwayElectrificationSystem()

    @Test fun `not applicable to railway nodes`() {
        val node = node( tags = mapOf("railway" to "rail") )
        assertEquals( false, questType.isApplicableTo(node))
    }

    @Test fun `not applicable to highway ways`() {
        val way = way(tags = mapOf("highway" to "motorway"))
        assertEquals( false, questType.isApplicableTo(way))
    }

    @Test fun `applicable to active track sections`() {
        val lightRail = way(tags = mapOf("railway" to "light_rail"))
        assertEquals(true, questType.isApplicableTo(lightRail))

        val narrowGauge = way(tags = mapOf("railway" to "narrow_gauge"))
        assertEquals(true, questType.isApplicableTo(narrowGauge))

        val preservedTrack = way(tags = mapOf("railway" to "preserved"))
        assertEquals(true, questType.isApplicableTo(preservedTrack))

        val regularTrack = way(tags = mapOf("railway" to "rail"))
        assertEquals(true, questType.isApplicableTo(regularTrack))

        val subway = way(tags = mapOf("railway" to "subway"))
        assertEquals(true, questType.isApplicableTo(subway))

        val tramTrack = way(tags = mapOf("railway" to "tram"))
        assertEquals(true, questType.isApplicableTo(tramTrack))
    }

    @Test fun `not applicable to tracks with electrification info`() {
        val nonElectrifiedTrack = way(tags = mapOf(
            "railway" to "rail",
            "electrified" to "no"
        ))
        assertEquals(false, questType.isApplicableTo(nonElectrifiedTrack))

        val electrifiedTrack = way(tags = mapOf(
            "railway" to "rail",
            "electrified" to "yes"
        ))
        assertEquals(false, questType.isApplicableTo(electrifiedTrack))

        val contactLineTrack = way(tags = mapOf(
            "railway" to "rail",
            "electrified" to "contact_line"
        ))
        assertEquals(false, questType.isApplicableTo(contactLineTrack))
    }
}
