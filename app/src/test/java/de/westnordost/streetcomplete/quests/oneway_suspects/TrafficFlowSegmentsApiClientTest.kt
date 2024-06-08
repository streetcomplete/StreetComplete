package de.westnordost.streetcomplete.quests.oneway_suspects

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegment
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class TrafficFlowSegmentsApiClientTest {
    private val boundingBox = BoundingBox(-34.0, 18.0, -33.0, 19.0)

    @Test fun `get with empty response does not result in error`(): Unit = runBlocking {
        val mockEngine = MockEngine { respondOk("""{"segments": []}""") }

        assertEquals(mapOf(), client(mockEngine).get(boundingBox))
    }

    @Test fun `get with two different ways`() = runBlocking {
        val mockEngine = MockEngine { respondOk("""
            {"segments":[
                {"wayId":1,"fromPosition":{"lon":1, "lat":2},"toPosition":{"lon":5, "lat":6}},
                {"wayId":2,"fromPosition":{"lon":3, "lat":4},"toPosition":{"lon":7, "lat":8}}
            ]}
        """) }

        assertEquals(
            mapOf(
                1L to listOf(TrafficFlowSegment(1L, LatLon(2.0, 1.0), LatLon(6.0, 5.0))),
                2L to listOf(TrafficFlowSegment(2L, LatLon(4.0, 3.0), LatLon(8.0, 7.0)))
            ),
            client(mockEngine).get(boundingBox)
        )
    }

    @Test fun `get with two of same way`() = runBlocking {
        val mockEngine = MockEngine { respondOk("""
            {"segments":[
                {"wayId":1,"fromPosition":{"lon":1, "lat":2},"toPosition":{"lon":5, "lat":6}},
                {"wayId":1,"fromPosition":{"lon":3, "lat":4},"toPosition":{"lon":7, "lat":8}}
            ]}
        """) }

        assertEquals(
            mapOf(1L to listOf(
                TrafficFlowSegment(1L, LatLon(2.0, 1.0), LatLon(6.0, 5.0)),
                TrafficFlowSegment(1L, LatLon(4.0, 3.0), LatLon(8.0, 7.0))
            )),
            client(mockEngine).get(boundingBox)
        )
    }

    @Test fun `get makes request to correct URL`(): Unit = runBlocking {
        val mockEngine = MockEngine { respondOk("""{"segments": []}""") }

        client(mockEngine).get(boundingBox)

        assertEquals(
            "http://example.com/?bbox=18.0000000,-34.0000000,19.0000000,-33.0000000",
            mockEngine.requestHistory[0].url.toString()
        )
    }

    private fun client(engine: HttpClientEngine) =
        TrafficFlowSegmentsApiClient(HttpClient(engine), "http://example.com/")
}
