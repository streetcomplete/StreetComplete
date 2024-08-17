package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.ApiClientException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StatisticsApiClientTest {
    private val statisticsParser: StatisticsParser = mock()

    private val validResponseMockEngine = MockEngine { respondOk("simple response") }

    @Test fun `download parses all statistics`() = runBlocking {
        val client = StatisticsApiClient(HttpClient(validResponseMockEngine), "", statisticsParser)
        val stats = Statistics(
            types = listOf(),
            countries = listOf(),
            rank = 2,
            daysActive = 100,
            currentWeekRank = 50,
            currentWeekTypes = listOf(),
            currentWeekCountries = listOf(),
            activeDates = listOf(),
            activeDatesRange = 100,
            isAnalyzing = false,
            lastUpdate = 10
        )
        on(statisticsParser.parse("simple response")).thenReturn(stats)
        assertEquals(stats, client.get(100))
    }

    @Test fun `download throws Exception for a 400 response`(): Unit = runBlocking {
        val mockEngine = MockEngine { _ -> respondBadRequest() }
        val client = StatisticsApiClient(HttpClient(mockEngine), "", statisticsParser)
        assertFailsWith<ApiClientException> { client.get(100) }
    }

    @Test fun `download constructs request URL`() = runBlocking {
        StatisticsApiClient(
            HttpClient(validResponseMockEngine),
            "https://example.com/stats/",
            statisticsParser
        ).get(100)

        assertEquals(
            "https://example.com/stats/?user_id=100",
            validResponseMockEngine.requestHistory[0].url.toString()
        )
    }
}
