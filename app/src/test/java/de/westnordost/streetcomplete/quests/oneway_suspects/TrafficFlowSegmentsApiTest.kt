package de.westnordost.streetcomplete.quests.oneway_suspects

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.oneway_suspects.data.ONEWAY_API_URL
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegment
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TrafficFlowSegmentsApiTest {
    private val boundingBox = BoundingBox(
        -34.0,
        18.0,
        -33.0,
        19.0
    )

    @Test fun `get with empty response does not result in error`(): Unit = runBlocking {
        val mockEngine = MockEngine { request -> respondOk("""{"segments": []}""") }

        assertEquals(mapOf(), TrafficFlowSegmentsApi(HttpClient(mockEngine), ONEWAY_API_URL).get(boundingBox))
    }

    @Test fun `get with two different ways`() = runBlocking {
        val mockEngine = MockEngine { request -> respondOk("""
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
            TrafficFlowSegmentsApi(HttpClient(mockEngine), ONEWAY_API_URL).get(boundingBox)
        )
    }

    @Test fun `get with two of same way`() = runBlocking {
        val mockEngine = MockEngine { request -> respondOk("""
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
            TrafficFlowSegmentsApi(HttpClient(mockEngine), ONEWAY_API_URL).get(boundingBox)
        )
    }

    @Test fun `get throws an ClientRequestException on a 400 response`(): Unit = runBlocking {
        val mockEngine = MockEngine { request -> respondBadRequest() }
        assertFailsWith<ClientRequestException> { TrafficFlowSegmentsApi(HttpClient(mockEngine), ONEWAY_API_URL).get(boundingBox) }
    }

    @Test fun `get throws an ServerResponseException on a 500 response`(): Unit = runBlocking {
        val mockEngine = MockEngine { request -> respondError(HttpStatusCode.InternalServerError) }
        assertFailsWith<ServerResponseException> { TrafficFlowSegmentsApi(HttpClient(mockEngine), ONEWAY_API_URL).get(boundingBox) }
    }

    @Test fun `get makes request to correct URL`(): Unit = runBlocking {
        val mockEngine = MockEngine { request -> respondOk("""{"segments": []}""") }

        TrafficFlowSegmentsApi(HttpClient(mockEngine), ONEWAY_API_URL).get(boundingBox)

        assertEquals(
            ONEWAY_API_URL + "?bbox=18.0000000,-34.0000000,19.0000000,-33.0000000",
            mockEngine.requestHistory[0].url.toString()
        )
    }
}
