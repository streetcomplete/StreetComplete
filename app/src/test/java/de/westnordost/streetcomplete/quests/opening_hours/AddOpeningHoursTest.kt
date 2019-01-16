package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

import org.mockito.Mockito.mock

class AddOpeningHoursTest {

    private val questType = AddOpeningHours(mock(OverpassMapDataDao::class.java))

    @Test fun `apply description answer`() {
        questType.verifyAnswer(
            DescribeOpeningHours("my cool \"opening\" hours"),
            StringMapEntryAdd("opening_hours", "\"my cool opening hours\"")
        )
    }

    @Test fun `apply no opening hours sign answer`() {
        questType.verifyAnswer(
            NoOpeningHoursSign,
            StringMapEntryAdd("opening_hours:signed", "no")
        )
    }

    @Test fun `apply always open answer`() {
        questType.verifyAnswer(
            AlwaysOpen,
            StringMapEntryAdd("opening_hours", "24/7")
        )
    }

    @Test fun `apply opening hours answer`() {
        questType.verifyAnswer(
            RegularOpeningHours(listOf(OpeningMonths(
                CircularSection(0,11),
                listOf(
                    listOf(
                        OpeningWeekdays(
                            Weekdays(booleanArrayOf(true)),
                            mutableListOf(TimeRange(0, 12*60))
                        )
                    ),
                    listOf(
                        OpeningWeekdays(
                            Weekdays(booleanArrayOf(false, true)),
                            mutableListOf(TimeRange(12*60, 24*60))
                        )
                    )
                )
            ))),
            StringMapEntryAdd("opening_hours", "Mo 00:00-12:00; Tu 12:00-24:00")
        )
    }
}
