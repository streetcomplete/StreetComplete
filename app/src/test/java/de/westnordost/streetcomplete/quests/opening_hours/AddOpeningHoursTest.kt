package de.westnordost.streetcomplete.quests.opening_hours

import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.TimeSpan
import ch.poole.openinghoursparser.WeekDay
import ch.poole.openinghoursparser.WeekDayRange
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test fun `isApplicableTo returns false for unknown places`() {
        assertFalse(questType.isApplicableTo(createElement(
            mapOf("whatisthis" to "something")
        )))
    }

    @Test fun `isApplicableTo returns true for known places`() {
        assertTrue(questType.isApplicableTo(createElement(
            mapOf("shop" to "sports", "name" to "Atze's Angelladen")
        )))
    }

    @Test fun `isApplicableTo returns true if the opening hours cannot be parsed`() {
        assertTrue(questType.isApplicableTo(createElement(
            mapOf("shop" to "supermarket", "name" to "Supi", "opening_hours" to "maybe open maybe closed who knows"),
            "2000-11-11".toCheckDate()
        )))
    }

    @Test fun `isApplicableTo returns false if the opening hours are not supported`() {
        assertFalse(questType.isApplicableTo(createElement(
            mapOf("shop" to "supermarket", "name" to "Supi", "opening_hours" to "1998 Mo-Fr 18:00-20:00"),
            "2000-11-11".toCheckDate()
        )))
    }

    private fun createElement(tags: Map<String, String>, date: Date? = null) : Node =
        OsmNode(1L, 1, OsmLatLon(0.0, 0.0), tags, null, date)
}
