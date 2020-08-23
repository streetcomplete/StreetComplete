package de.westnordost.streetcomplete.quests.opening_hours

import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.TimeSpan
import ch.poole.openinghoursparser.WeekDay
import ch.poole.openinghoursparser.WeekDayRange
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test


class AddOpeningHoursTest {

    private val questType = AddOpeningHours(mock(), mock(), mock())

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
            RegularOpeningHours(OpeningHoursRuleList(listOf(
                Rule().apply {
                    days = listOf(WeekDayRange().also {
                        it.startDay = WeekDay.MO
                    })
                    times = listOf(TimeSpan().also {
                        it.start = 60*10
                        it.end = 60*12
                    })
                },
                Rule().apply {
                    days = listOf(WeekDayRange().also {
                        it.startDay = WeekDay.TU
                    })
                    times = listOf(TimeSpan().also {
                        it.start = 60*12
                        it.end = 60*24
                    })
                })
            )),
            StringMapEntryAdd("opening_hours", "Mo 10:00-12:00; Tu 12:00-24:00")
        )
    }
}
