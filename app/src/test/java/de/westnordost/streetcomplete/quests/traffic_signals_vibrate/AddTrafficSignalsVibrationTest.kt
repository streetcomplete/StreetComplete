package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

class AddTrafficSignalsVibrationTest {
    private val questType = AddTrafficSignalsVibration()

    @Test fun `applicable to crossing`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, mapOf(
                "highway" to "crossing",
                "crossing" to "traffic_signals"
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to crossing of cycleway without foot access`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, mapOf(
                "highway" to "crossing",
                "crossing" to "traffic_signals"
            )),
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

}
