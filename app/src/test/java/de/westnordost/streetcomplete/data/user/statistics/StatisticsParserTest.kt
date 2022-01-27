package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeC
import java.time.OffsetDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsParserTest {

    private val questA = TestQuestTypeA()
    private val questB = TestQuestTypeB()
    private val questC = TestQuestTypeC()
    private val registry = QuestTypeRegistry(listOf(questA, questB, questC))

    @Test fun `parse all`() {

        assertEquals(Statistics(
            questTypes = listOf(
                QuestTypeStatistics(questA, 11),
                QuestTypeStatistics(questB, 4),
                QuestTypeStatistics(questC, 45),
            ),
            countries = listOf(
                CountryStatistics("DE", 8, null),
                CountryStatistics("US", 7, 123),
            ),
            2345,
            78,
            OffsetDateTime.parse("2007-12-03T10:15:30+01:00").toInstant().toEpochMilli(),
            false
        ),
        StatisticsParser(registry, listOf("TestQuestTypeCAlias" to "TestQuestTypeC")).parse("""
        {
            "questTypes": {
                "TestQuestTypeA": "11",
                "TestQuestTypeB": "4",
                "UnknownQuestType": "111",
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
