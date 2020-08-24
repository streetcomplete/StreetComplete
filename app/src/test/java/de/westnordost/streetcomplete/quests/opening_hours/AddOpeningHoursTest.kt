package de.westnordost.streetcomplete.quests.opening_hours

import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.TimeSpan
import ch.poole.openinghoursparser.WeekDay
import ch.poole.openinghoursparser.WeekDayRange
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test
import java.util.*


class AddOpeningHoursTest {

    private val questType = AddOpeningHours(mock(), mock(), mock())

    @Test fun `apply description answer`() {
        questType.verifyAnswer(
            DescribeOpeningHours("my cool \"opening\" hours"),
            StringMapEntryAdd("opening_hours", "\"my cool opening hours\"")
        )
    }

    @Test fun `apply description answer when it already had an opening hours`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "my opening hours"),
            DescribeOpeningHours("my cool \"opening\" hours"),
            StringMapEntryModify("opening_hours", "my opening hours", "\"my cool opening hours\"")
        )
    }

    @Test fun `apply same description answer again`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "\"oh\""),
            DescribeOpeningHours("oh"),
            StringMapEntryAdd("check_date:opening_hours", Date().toCheckDateString())
        )
    }

    @Test fun `apply no opening hours sign answer`() {
        questType.verifyAnswer(
            NoOpeningHoursSign,
            StringMapEntryAdd("opening_hours:signed", "no")
        )
    }

    @Test fun `apply no opening hours sign answer when there was an answer before`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "oh"),
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

    @Test fun `apply always open answer when there was a different answer before`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "34/3"),
            AlwaysOpen,
            StringMapEntryModify("opening_hours", "34/3", "24/7")
        )
    }

    @Test fun `apply always open answer when it was the same answer before`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "24/7"),
            AlwaysOpen,
            StringMapEntryAdd("check_date:opening_hours", Date().toCheckDateString())
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

    @Test fun `apply opening hours answer when there was a different one before`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "hohoho"),
            RegularOpeningHours(OpeningHoursRuleList(listOf(
                Rule().apply {
                    days = listOf(WeekDayRange().also {
                        it.startDay = WeekDay.MO
                    })
                    times = listOf(TimeSpan().also {
                        it.start = 60*10
                        it.end = 60*12
                    })
                })
            )),
            StringMapEntryModify("opening_hours", "hohoho","Mo 10:00-12:00")
        )
    }

    @Test fun `apply opening hours answer when there was the same one before`() {
        questType.verifyAnswer(
            mapOf("opening_hours" to "Mo 10:00-12:00"),
            RegularOpeningHours(OpeningHoursRuleList(listOf(
                Rule().apply {
                    days = listOf(WeekDayRange().also {
                        it.startDay = WeekDay.MO
                    })
                    times = listOf(TimeSpan().also {
                        it.start = 60*10
                        it.end = 60*12
                    })
                })
            )),
            StringMapEntryAdd("check_date:opening_hours", Date().toCheckDateString())
        )
    }
}
