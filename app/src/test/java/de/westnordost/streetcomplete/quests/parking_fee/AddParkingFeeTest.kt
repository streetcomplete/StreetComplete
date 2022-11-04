package de.westnordost.streetcomplete.quests.parking_fee

import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.TimeSpan
import ch.poole.openinghoursparser.WeekDay
import ch.poole.openinghoursparser.WeekDayRange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.opening_hours.parser.OpeningHoursRuleList
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddParkingFeeTest {

    private val questType = AddParkingFee()

    private val openingHours = OpeningHoursRuleList(listOf(
        Rule().apply {
            days = listOf(WeekDayRange().also {
                it.startDay = WeekDay.MO
            })
            times = listOf(TimeSpan().also {
                it.start = 60 * 10
                it.end = 60 * 12
            })
        },
        Rule().apply {
            days = listOf(WeekDayRange().also {
                it.startDay = WeekDay.TU
            })
            times = listOf(TimeSpan().also {
                it.start = 60 * 12
                it.end = 60 * 24
            })
        })
    )
    private val openingHoursString = "Mo 10:00-12:00; Tu 12:00-24:00"

    @Test fun `apply yes answer`() {
        questType.verifyAnswer(FeeAndMaxStay(HasFee), StringMapEntryAdd("fee", "yes"))
    }

    @Test fun `apply no answer`() {
        questType.verifyAnswer(FeeAndMaxStay(HasNoFee), StringMapEntryAdd("fee", "no"))
    }

    @Test fun `apply only at hours answer`() {
        questType.verifyAnswer(
            FeeAndMaxStay(HasFeeAtHours(openingHours)),
            StringMapEntryAdd("fee", "no"),
            StringMapEntryAdd("fee:conditional", "yes @ ($openingHoursString)")
        )
    }

    @Test fun `apply yes except at hours answer`() {
        questType.verifyAnswer(
            FeeAndMaxStay(HasFeeExceptAtHours(openingHours)),
            StringMapEntryAdd("fee", "yes"),
            StringMapEntryAdd("fee:conditional", "no @ ($openingHoursString)")
        )
    }

    @Test fun `apply yes answer if before was conditional`() {
        questType.verifyAnswer(
            mapOf("fee:conditional" to "someval", "fee" to "no"),
            FeeAndMaxStay(HasFee),
            StringMapEntryModify("fee", "no", "yes"),
            StringMapEntryDelete("fee:conditional", "someval")
        )
    }

    @Test fun `apply conditional answer if before was yes`() {
        questType.verifyAnswer(
            mapOf("fee" to "yes"),
            FeeAndMaxStay(HasFeeExceptAtHours(openingHours)),
            StringMapEntryModify("fee", "yes", "yes"),
            StringMapEntryAdd("fee:conditional", "no @ ($openingHoursString)"),
            StringMapEntryAdd("check_date:fee", nowAsCheckDateString())
        )
    }

    @Test fun `apply no but maxstay answer`() {
        questType.verifyAnswer(
            FeeAndMaxStay(HasNoFee, MaxstayDuration(2.5, Maxstay.Unit.HOURS)),
            StringMapEntryAdd("fee", "no"),
            StringMapEntryAdd("maxstay", "2.5 hours")
        )
    }

    @Test fun `apply no but maxstay at hours answer`() {
        questType.verifyAnswer(
            FeeAndMaxStay(HasNoFee, MaxstayAtHours(MaxstayDuration(30.0, Maxstay.Unit.MINUTES), openingHours)),
            StringMapEntryAdd("fee", "no"),
            StringMapEntryAdd("maxstay", "no"),
            StringMapEntryAdd("maxstay:conditional", "30 minutes @ ($openingHoursString)"),
        )
    }

    @Test fun `apply no but maxstay except at hours answer`() {
        questType.verifyAnswer(
            FeeAndMaxStay(HasNoFee, MaxstayExceptAtHours(MaxstayDuration(1.0, Maxstay.Unit.DAYS), openingHours)),
            StringMapEntryAdd("fee", "no"),
            StringMapEntryAdd("maxstay", "1 day"),
            StringMapEntryAdd("maxstay:conditional", "no @ ($openingHoursString)"),
        )
    }

    @Test fun `apply no but maxstay if before was fee yes`() {
        questType.verifyAnswer(
            mapOf("fee" to "yes", "maxstay" to "unlimited", "maxstay:conditional" to "3 hours @ (10:00-20:00)"),
            FeeAndMaxStay(HasNoFee, MaxstayDuration(1.0, Maxstay.Unit.HOURS)),
            StringMapEntryModify("fee", "yes", "no"),
            StringMapEntryDelete("maxstay:conditional", "3 hours @ (10:00-20:00)"),
            StringMapEntryModify("maxstay", "unlimited", "1 hour"),
        )
    }

    @Test fun `apply no but maxstay if before it was the same`() {
        questType.verifyAnswer(
            mapOf("fee" to "no", "maxstay" to "no", "maxstay:conditional" to "1 hour @ ($openingHoursString)"),
            FeeAndMaxStay(HasNoFee, MaxstayAtHours(MaxstayDuration(1.0, Maxstay.Unit.HOURS), openingHours)),
            StringMapEntryModify("fee", "no", "no"),
            StringMapEntryAdd("check_date:fee", nowAsCheckDateString()),
            StringMapEntryModify("maxstay:conditional", "1 hour @ ($openingHoursString)", "1 hour @ ($openingHoursString)"),
            StringMapEntryModify("maxstay", "no", "no"),
            StringMapEntryAdd("check_date:maxstay", nowAsCheckDateString())
        )
    }
}
