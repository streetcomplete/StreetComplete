package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

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
            2345,
            78,
            Instant.parse("2007-12-03T10:15:30+01:00").toEpochMilliseconds(),
            false
        ),
        StatisticsParser(listOf("TestQuestTypeCAlias" to "TestQuestTypeC")).parse("""
        {
            "questTypes": {
                "TestQuestTypeA": "11",
                "TestQuestTypeB": "4",
                "TestQuestTypeCAlias": "45",
            },
            "countries": {
                "DE": "8",
                "US": "7",
            },
            "countryRanks": {
                "US": "123",
            },
            "rank": "2345",
            "daysActive": "78",
            "lastUpdate": "2007-12-03T10:15:30+01:00",
            "isAnalyzing": "false"
        }
        """))
    }
}
