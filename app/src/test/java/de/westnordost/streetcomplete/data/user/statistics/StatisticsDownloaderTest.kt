package de.westnordost.streetcomplete.data.user.statistics

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondOk
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class StatisticsDownloaderTest {
    @Mock
    private val statisticsParser: StatisticsParser = mock(classOf<StatisticsParser>())

    private val validResponseMockEngine = MockEngine { _ -> respondOk("simple response") }

    @Test fun `download parses all statistics`() = runBlocking {
        val stats = Statistics(types = listOf(), countries = listOf(), rank = 2, daysActive = 100, currentWeekRank = 50, currentWeekTypes = listOf(), currentWeekCountries = listOf(), activeDates = listOf(), activeDatesRange = 100, isAnalyzing = false, lastUpdate = 10)
        every { statisticsParser.parse("simple response") }.returns(stats)
        assertEquals(stats, StatisticsDownloader(HttpClient(validResponseMockEngine), "", statisticsParser).download(100))
    }

    @Test fun `download throws Exception for a 400 response`() = runBlocking {
        val mockEngine = MockEngine { _ -> respondBadRequest() }
        val exception = assertFails { StatisticsDownloader(HttpClient(mockEngine), "", statisticsParser).download(100) }

        assertEquals(
            "Client request(GET http://localhost/?user_id=100) invalid: 400 Bad Request. Text: \"Bad Request\"",
            exception.message
        )
    }

    @Test fun `download constructs request URL`() = runBlocking {
        val stats = Statistics(types = listOf(), countries = listOf(), rank = 2, daysActive = 100, currentWeekRank = 50, currentWeekTypes = listOf(), currentWeekCountries = listOf(), activeDates = listOf(), activeDatesRange = 100, isAnalyzing = false, lastUpdate = 10)
        every { statisticsParser.parse(any()) }.returns(stats)

        StatisticsDownloader(
            HttpClient(validResponseMockEngine),
            "https://example.com/stats/",
            statisticsParser
        ).download(100)

        assertEquals("https://example.com/stats/?user_id=100", validResponseMockEngine.requestHistory[0].url.toString())
    }
}
