package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.*
import org.junit.Test

class AddTrafficSignalsVibrationTest {
    private val questType = AddTrafficSignalsVibration()

    @Test fun `applicable to crossing`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(tags = mapOf(
                "highway" to "crossing",
                "crossing" to "traffic_signals"
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to crossing of cycleway without foot access`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, tags = mapOf(
                "highway" to "crossing",
                "crossing" to "traffic_signals"
            )),
            way(1, listOf(1,2,3), mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

}
