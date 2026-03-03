package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.Months
import de.westnordost.streetcomplete.osm.opening_hours.Times
import de.westnordost.streetcomplete.osm.opening_hours.Weekdays
import de.westnordost.streetcomplete.osm.toCheckDate
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddOpeningHoursTest {

    private val questType = AddOpeningHours()

    @Test fun `apply description answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("opening_hours", "\"my cool opening hours\"")),
            questType.answerApplied(DescribeOpeningHours("my cool \"opening\" hours"))
        )
    }

    @Test fun `apply description answer when it already had an opening hours`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours", "X", "\"my cool opening hours\"")
            ),
            questType.answerAppliedTo(
                DescribeOpeningHours("my cool \"opening\" hours"),
                mapOf("opening_hours" to "X")
            )
        )
    }

    @Test fun `apply same description answer again`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours", "\"oh\"", "\"oh\""),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                DescribeOpeningHours("oh"),
                mapOf("opening_hours" to "\"oh\"")
            )
        )
    }

    @Test fun `apply no opening hours sign answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("opening_hours:signed", "no"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerApplied(NoOpeningHoursSign)
        )
    }

    @Test fun `apply no opening hours sign answer when there was an answer before`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("opening_hours:signed", "no"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                NoOpeningHoursSign,
                mapOf("opening_hours" to "oh")
            )
        )
    }

    @Test fun `apply no opening hours sign answer when there was an always open answer before`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("opening_hours:signed", "no"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(NoOpeningHoursSign, mapOf("opening_hours" to "24/7"))
        )
    }

    @Test fun `apply always open answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("opening_hours", "24/7")),
            questType.answerApplied(AlwaysOpen)
        )
    }

    @Test fun `apply always open answer when there was a different answer before`() {
        assertEquals(
            setOf(StringMapEntryModify("opening_hours", "34/3", "24/7")),
            questType.answerAppliedTo(AlwaysOpen, mapOf("opening_hours" to "34/3"))
        )
    }

    @Test fun `apply always open answer when it was the same answer before`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours", "24/7", "24/7"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(AlwaysOpen, mapOf("opening_hours" to "24/7"))
        )
    }

    @Test fun `apply always open answer when it was explicitly signed before`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("opening_hours", "24/7")
            ),
            questType.answerAppliedTo(AlwaysOpen, mapOf("opening_hours:signed" to "yes"))
        )
    }

    @Test fun `apply always open answer when it was explicitly signed and present before`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours", "24/7", "24/7"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                AlwaysOpen,
                mapOf(
                    "opening_hours" to "24/7",
                    "opening_hours:signed" to "yes"
                )
            )
        )
    }

    @Test fun `apply always open answer when it was explicitly signed but there was a different answer before`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours", "34/3", "24/7")
            ),
            questType.answerAppliedTo(
                AlwaysOpen,
                mapOf(
                    "opening_hours" to "34/3",
                    "opening_hours:signed" to "yes"
                )
            )
        )
    }

    @Test fun `apply opening hours answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("opening_hours", "Mo 10:00-12:00")),
            questType.answerApplied(
                RegularOpeningHours(HierarchicOpeningHours(listOf(
                    Months(emptyList(), listOf(
                        Weekdays(listOf(Weekday.Monday), emptyList(),
                            Times(listOf(TimeSpan(ClockTime(10), ClockTime(12))))
                        )
                    ))
                )))
            )
        )
    }

    @Test fun `apply opening hours answer when there was a different one before`() {
        assertEquals(
            setOf(StringMapEntryModify("opening_hours", "hohoho", "Mo 10:00-12:00")),
            questType.answerAppliedTo(
                RegularOpeningHours(HierarchicOpeningHours(listOf(
                    Months(emptyList(), listOf(
                        Weekdays(listOf(Weekday.Monday), emptyList(),
                            Times(listOf(TimeSpan(ClockTime(10), ClockTime(12))))
                        )
                    ))
                ))),
                mapOf("opening_hours" to "hohoho")
            )
        )
    }

    @Test fun `apply opening hours answer when there was the same one before`() {
        assertEquals(
            setOf(
                StringMapEntryModify("opening_hours", "Mo 10:00-12:00", "Mo 10:00-12:00"),
                StringMapEntryAdd("check_date:opening_hours", nowAsCheckDateString())
            ),
            questType.answerAppliedTo(
                RegularOpeningHours(HierarchicOpeningHours(listOf(
                    Months(emptyList(), listOf(
                        Weekdays(listOf(Weekday.Monday), emptyList(),
                            Times(listOf(TimeSpan(ClockTime(10), ClockTime(12))))
                        )
                    ))
                ))),
                mapOf("opening_hours" to "Mo 10:00-12:00")
            )
        )
    }

    @Test fun `isApplicableTo returns false for unknown places`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf("whatisthis" to "something")
        )))
    }

    @Test fun `isApplicableTo returns true for named places`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf("shop" to "sports", "name" to "Atze's Angelladen")
        )))
    }

    @Test fun `isApplicableTo returns false for unnamed places`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf("shop" to "sports")
        )))
    }

    @Test fun `isApplicableTo returns false for places unsupported by idtagging`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf("barrier" to "unsupported", "opening_hours" to "Mo-Fr 10:00-20:00")
        )))
    }

    @Test fun `isApplicableTo returns true for places with brands`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf("shop" to "sports", "brand" to "Atze's Angelladen")
        )))
    }

    @Test fun `isApplicableTo returns true for places noname=yes`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf("shop" to "sports", "noname" to "yes")
        )))
    }

    @Test fun `isApplicableTo returns true for places namesigned=no`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf("shop" to "sports", "name:signed" to "no")
        )))
    }

    @Test fun `isApplicableTo returns true for unnamed parking`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf("amenity" to "bicycle_parking", "bicycle_parking" to "building")
        )))
    }

    @Test fun `isApplicableTo returns false for known places with recently edited opening hours`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("shop" to "sports", "name" to "Atze's Angelladen", "opening_hours" to "Mo-Fr 10:00-20:00"), timestamp = nowAsEpochMilliseconds())
        ))
    }

    @Test fun `isApplicableTo returns true for known places with old opening hours`() {
        val millisecondsFor400Days: Long = 1000L * 60 * 60 * 24 * 400
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("shop" to "sports", "name" to "Atze's Angelladen", "opening_hours" to "Mo-Fr 10:00-20:00"), timestamp = nowAsEpochMilliseconds() - millisecondsFor400Days)
        ))
    }

    @Test fun `isApplicableTo returns false for closed shops with old opening hours`() {
        val millisecondsFor400Days: Long = 1000L * 60 * 60 * 24 * 400
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("nonexisting:shop" to "sports", "name" to "Atze's Angelladen", "opening_hours" to "Mo-Fr 10:00-20:00"), timestamp = nowAsEpochMilliseconds() - millisecondsFor400Days)
        ))
    }

    @Test fun `isApplicableTo returns true for parks with old opening hours`() {
        val millisecondsFor400Days: Long = 1000L * 60 * 60 * 24 * 400
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("leisure" to "park", "opening_hours" to "Mo-Fr 10:00-20:00"), timestamp = nowAsEpochMilliseconds() - millisecondsFor400Days)
        ))
    }

    @Test fun `isApplicableTo returns false for toilets without opening hours or fee`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "toilets"), timestamp = nowAsEpochMilliseconds())
        ))
    }

    @Test fun `isApplicableTo returns true for toilets with fee=yes`() {
        assertTrue(questType.isApplicableTo(
            node(tags = mapOf("amenity" to "toilets", "fee" to "yes"), timestamp = nowAsEpochMilliseconds())
        ))
    }

    @Test fun `isApplicableTo returns true if the opening hours cannot be parsed`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf(
                "shop" to "supermarket",
                "name" to "Supi",
                "opening_hours" to "maybe open maybe closed who knows"
            ),
            timestamp = "2000-11-11".toCheckDate()?.toEpochMilli()
        )))
    }

    @Test fun `isApplicableTo returns true if the opening hours collide with themselves`() {
        assertTrue(questType.isApplicableTo(node(
            tags = mapOf(
                "shop" to "supermarket",
                "name" to "Supi",
                "opening_hours" to "Mo-Fr 18:00-20:00; We 08:00-12:00"
            ),
            timestamp = "2000-11-11".toCheckDate()?.toEpochMilli()
        )))
    }

    @Test fun `isApplicableTo returns false if the opening hours are not supported`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf(
                "shop" to "supermarket",
                "name" to "Supi",
                "opening_hours" to "1998 Mo-Fr 18:00-20:00"
            ),
            timestamp = "2000-11-11".toCheckDate()?.toEpochMilli()
        )))
    }

    @Test fun `isApplicableTo returns false if the opening hours are not signed`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf(
                "shop" to "supermarket",
                "name" to "Supi",
                "opening_hours:signed" to "no"
            ),
            timestamp = "2000-11-11".toCheckDate()?.toEpochMilli()
        )))
    }

    @Test fun `isApplicableTo returns false if the opening hours are not signed, even if there are actually some set`() {
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf(
                "shop" to "supermarket",
                "name" to "Supi",
                "opening_hours" to "24/7",
                "opening_hours:signed" to "no"
            ),
            timestamp = "2000-11-11".toCheckDate()?.toEpochMilli()
        )))
    }

    @Test fun `isApplicableTo returns false for valid but not supported opening hours with ranges that are also colliding`() {
        val millisecondsFor400Days: Long = 1000L * 60 * 60 * 24 * 400
        assertFalse(questType.isApplicableTo(node(
            tags = mapOf(
                "shop" to "sports",
                "name" to "Atze's Angelladen",
                "opening_hours" to "\"by appointment, see timetable\"; PH \"by appointment\""
            ),
            timestamp = nowAsEpochMilliseconds() - millisecondsFor400Days
        )))
    }
}
