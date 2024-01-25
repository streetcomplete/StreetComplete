package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsParserTest {

    private val questA = "TestQuestTypeA"
    private val questB = "TestQuestTypeB"
    private val questC = "TestQuestTypeC"

    @Test fun `parse all`() {
        assertEquals(Statistics(
            types = listOf(
                EditTypeStatistics(questA, 11),
                EditTypeStatistics(questB, 4),
                EditTypeStatistics(questC, 45),
            ),
            countries = listOf(
                CountryStatistics("DE", 8, null),
                CountryStatistics("US", 7, 123),
            ),
            rank = 2345,
            daysActive = 78,
            currentWeekRank = 3,
            currentWeekTypes = listOf(
                EditTypeStatistics(questA, 9),
                EditTypeStatistics(questB, 99),
                EditTypeStatistics(questC, 999),
            ),
            currentWeekCountries = listOf(
                CountryStatistics("AT", 5, 666),
                CountryStatistics("IT", 4, null),
            ),
            activeDatesRange = 45,
            activeDates = listOf(
                LocalDate.parse("2011-08-07"), LocalDate.parse("2012-12-09")
            ),
            lastUpdate = Instant.parse("2007-12-03T10:15:30+01:00").toEpochMilliseconds(),
            isAnalyzing = false
        ),
        StatisticsParser(listOf("TestQuestTypeCAlias" to "TestQuestTypeC")).parse("""
        {
            "questTypes": {
                "TestQuestTypeA": "11",
                "TestQuestTypeB": "4",
                "TestQuestTypeCAlias": "45"
            },
            "countries": {
                "DE": "8",
                "US": "7"
            },
            "countryRanks": {
                "US": "123"
            },
            "rank": "2345",
            "currentWeekRank": "3",
            "currentWeekQuestTypes": {
                "TestQuestTypeA": "9",
                "TestQuestTypeB": "99",
                "TestQuestTypeCAlias": "999"
            },
            "currentWeekCountries": {
                "IT": 4,
                "AT": 5
            },
            "currentWeekCountryRanks": {
                "AT": 666
            },
            "daysActive": "78",
            "activeDatesRange": "45",
            "activeDates": ["2011-08-07", "2012-12-09"],
            "lastUpdate": "2007-12-03T10:15:30+01:00",
            "isAnalyzing": false
        }
        """))
    }
}
