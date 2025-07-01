package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.ApiClientException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.atpEntry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.util.logs.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AtpApiClientTest {
    private val apiParser: AtpApiParser = mock()

    private val validResponseMockEngine = MockEngine { respondOk("simple response") }

    @Test fun `download parses all atp entries`() = runBlocking {
        val bounds = BoundingBox(0.1, 0.1, 0.2, 0.2)
        val client = AtpApiClient(HttpClient(validResponseMockEngine), "", apiParser)
        val response = listOf(atpEntry(), atpEntry())
        on(apiParser.parseAtpEntries(any())).thenReturn(response)
        assertEquals(response, client.getAllAtpEntries(bounds))
    }

    @Test fun `download throws Exception for a 400 response`(): Unit = runBlocking {
        val bounds = BoundingBox(0.1, 0.1, 0.2, 0.2)
        val mockEngine = MockEngine { _ -> respondBadRequest() }
        val client = AtpApiClient(HttpClient(mockEngine), "", apiParser)
        assertFailsWith<ApiClientException> { client.getAllAtpEntries(bounds) }
    }

    @Test fun `download handles split into two graticules on longitude axis`() = runBlocking {
        val bounds = BoundingBox(0.1, -0.1, 0.2, 0.1)
        val splitGraticulesResponseMockEngine = MockEngine { request ->
            when (request.url.toString()) {
                "http://localhost/lat_0/lon_-1_gathered.geojson" -> respondOk("NEGATIVE_LONGITUDE_DATA")
                "http://localhost/lat_0/lon_0_gathered.geojson" -> respondOk("ZERO_LONGITUDE_DATA")
                else -> {
                    respondBadRequest()
                }
            }
        }

        val negative = atpEntry(id = 1)
        val positive = atpEntry(id = 2)
        on(apiParser.parseAtpEntries(any())).thenAnswer { invocation ->
            val source = invocation.getArgument<Source>(0)

            when (source.readString()) {
                "NEGATIVE_LONGITUDE_DATA" -> listOf(negative)
                "ZERO_LONGITUDE_DATA" -> listOf(positive)
                else -> null
            }
        }
        val client = AtpApiClient(HttpClient(splitGraticulesResponseMockEngine), "", apiParser)
        val response = listOf(negative, positive)

        assertEquals(response, client.getAllAtpEntries(bounds))
    }

    @Test fun `download handles split into four graticules`() = runBlocking {
        val bounds = BoundingBox(19.9, 49.9, 20.2, 50.1)
        val splitGraticulesResponseMockEngine = MockEngine { request ->
            when (request.url.toString()) {
                "http://localhost/lat_19/lon_49_gathered.geojson" -> respondOk("MIN_LAT_MIN_LON")
                "http://localhost/lat_19/lon_50_gathered.geojson" -> respondOk("MIN_LAT_MAX_LON")
                "http://localhost/lat_20/lon_49_gathered.geojson" -> respondOk("MAX_LAT_MIN_LON")
                "http://localhost/lat_20/lon_50_gathered.geojson" -> respondOk("MAX_LAT_MAX_LON")
                else -> {
                    respondBadRequest()
                }
            }
        }

        val first = atpEntry(id = 1)
        val second = atpEntry(id = 2)
        val third = atpEntry(id = 3)
        val fourth = atpEntry(id = 4)
        on(apiParser.parseAtpEntries(any())).thenAnswer { invocation ->
            val source = invocation.getArgument<Source>(0)

            when (source.readString()) {
                "MIN_LAT_MIN_LON" -> listOf(first)
                "MIN_LAT_MAX_LON" -> listOf(second)
                "MAX_LAT_MIN_LON" -> listOf(third)
                "MAX_LAT_MAX_LON" -> listOf(fourth)
                else -> null
            }
        }
        val client = AtpApiClient(HttpClient(splitGraticulesResponseMockEngine), "", apiParser)
        val response = listOf(first, second, third, fourth)

        assertEquals(response, client.getAllAtpEntries(bounds))
    }

    @Test fun `download handles split into two graticules on latitude axis`() = runBlocking {
        val bounds = BoundingBox(19.9, 50.8, 20.2, 50.9)
        val splitGraticulesResponseMockEngine = MockEngine { request ->
            when (request.url.toString()) {
                "http://localhost/lat_19/lon_49_gathered.geojson" -> respondOk("MIN_LAT_MIN_LON")
                "http://localhost/lat_19/lon_50_gathered.geojson" -> respondOk("MIN_LAT_MAX_LON")
                "http://localhost/lat_20/lon_49_gathered.geojson" -> respondOk("MAX_LAT_MIN_LON")
                "http://localhost/lat_20/lon_50_gathered.geojson" -> respondOk("MAX_LAT_MAX_LON")
                else -> {
                    respondBadRequest()
                }
            }
        }

        val first = atpEntry(id = 1)
        val second = atpEntry(id = 2)
        val third = atpEntry(id = 3)
        val fourth = atpEntry(id = 4)
        on(apiParser.parseAtpEntries(any())).thenAnswer { invocation ->
            val source = invocation.getArgument<Source>(0)

            when (source.readString()) {
                "MIN_LAT_MIN_LON" -> listOf(first)
                "MIN_LAT_MAX_LON" -> listOf(second)
                "MAX_LAT_MIN_LON" -> listOf(third)
                "MAX_LAT_MAX_LON" -> listOf(fourth)
                else -> null
            }
        }
        val client = AtpApiClient(HttpClient(splitGraticulesResponseMockEngine), "", apiParser)
        val response = listOf(second, fourth)

        assertEquals(response, client.getAllAtpEntries(bounds))
    }
}
