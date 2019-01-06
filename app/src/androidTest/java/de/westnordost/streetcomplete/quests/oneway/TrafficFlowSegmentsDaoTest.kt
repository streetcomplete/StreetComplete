package de.westnordost.streetcomplete.quests.oneway

import junit.framework.TestCase

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegment
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao

import de.westnordost.streetcomplete.data.OsmModule.ONEWAY_API_URL
import org.assertj.core.api.Assertions.*

class TrafficFlowSegmentsDaoTest : TestCase() {

    fun testParseEmptyDoesNotResultInError() {
        val result = TrafficFlowSegmentsDao.parse("{\"segments\":[]}")
        assertTrue(result.isEmpty())
    }

    fun testParseTwoOfDifferentWay() {
        val result = TrafficFlowSegmentsDao.parse("""
            {"segments":[
                {"wayId":1,"fromPosition":{"lon":1, "lat":2},"toPosition":{"lon":5, "lat":6}},
                {"wayId":2,"fromPosition":{"lon":3, "lat":4},"toPosition":{"lon":7, "lat":8}},
            ]}""".trimIndent()
        )
        val expected = mapOf(
            1L to listOf(TrafficFlowSegment(OsmLatLon(2.0, 1.0), OsmLatLon(6.0, 5.0))),
            2L to listOf(TrafficFlowSegment(OsmLatLon(4.0, 3.0), OsmLatLon(8.0, 7.0)))
        )
        assertThat(result).containsAllEntriesOf(expected)
    }

    fun testParseTwoOfSameWay() {
        val result = TrafficFlowSegmentsDao.parse("""
            {"segments":[
                {"wayId":1,"fromPosition":{"lon":1, "lat":2},"toPosition":{"lon":5, "lat":6}},
                {"wayId":1,"fromPosition":{"lon":3, "lat":4},"toPosition":{"lon":7, "lat":8}},
            ]}""".trimIndent()
        )
        val expected = mapOf(1L to listOf(
            TrafficFlowSegment(OsmLatLon(2.0, 1.0), OsmLatLon(6.0, 5.0)),
            TrafficFlowSegment(OsmLatLon(4.0, 3.0), OsmLatLon(8.0, 7.0))
        ))
        assertThat(result).containsAllEntriesOf(expected)
    }

    fun testWithSomeRealData() {
        // should just not crash...
        TrafficFlowSegmentsDao(ONEWAY_API_URL).get(BoundingBox(-34.0, 18.0, -33.0, 19.0))
    }
}
