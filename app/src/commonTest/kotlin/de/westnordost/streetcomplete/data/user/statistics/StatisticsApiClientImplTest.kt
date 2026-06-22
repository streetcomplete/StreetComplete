package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.ApiClientException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class StatisticsApiClientImplTest {
    private val statisticsParser = StatisticsParser(emptyList())

    private val validResponseMockEngine = MockEngine { respondOk("""
        {
            "questTypes": { "Abc": 77, "Def": 88 },
            "countries": { "DE": 2 },
            "countryRanks": { },
            "rank": 2,
            "daysActive": 100,
            "currentWeekRank": 50,
            "currentWeekQuestTypes": { "Abc": 4 },
            "currentWeekCountries": { "FR": 1 },
            "currentWeekCountryRanks": { "FR": 123 },
            "activeDates": ["2000-12-10"],
            "activeDatesRange": 100,
            "isAnalyzing": false,
            "lastUpdate": "2007-12-03T10:15:30+01:00"
        }
        """.trimIndent())
    }

    @Test fun `download parses all statistics`() = runBlocking {
        val client = StatisticsApiClientImpl(HttpClient(validResponseMockEngine), "", statisticsParser)
        val stats = Statistics(
            types = listOf(EditTypeStatistics("Abc", 77), EditTypeStatistics("Def", 88)),
            countries = listOf(CountryStatistics("DE", 2, null)),
            rank = 2,
            daysActive = 100,
            currentWeekRank = 50,
            currentWeekTypes = listOf(EditTypeStatistics("Abc", 4)),
            currentWeekCountries = listOf(CountryStatistics("FR", 1, 123)),
            activeDates = listOf(LocalDate(2000,12,10)),
            activeDatesRange = 100,
            isAnalyzing = false,
            lastUpdate = Instant.parse("2007-12-03T10:15:30+01:00").toEpochMilliseconds()
        )
        assertEquals(stats, client.get(100))
    }

    @Test fun `download throws Exception for a 400 response`(): Unit = runBlocking {
        val mockEngine = MockEngine { _ -> respondBadRequest() }
        val client = StatisticsApiClientImpl(HttpClient(mockEngine), "", statisticsParser)
        assertFailsWith<ApiClientException> { client.get(100) }
    }

    @Test fun `download constructs request URL`() = runBlocking {
        StatisticsApiClientImpl(
            httpClient = HttpClient(validResponseMockEngine),
            baseUrl = "https://example.com/stats/",
            statisticsParser = statisticsParser
        ).get(100)

        assertEquals(
            "https://example.com/stats/?user_id=100",
            validResponseMockEngine.requestHistory[0].url.toString()
        )
    }
}
