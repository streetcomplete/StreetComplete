package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.parking_fee.*
import org.junit.Test

class AddParkingFeeTest {

    private val questType = AddParkingFee(mock())


    private val openingHours = listOf(
            OpeningMonthsRow(CircularSection(0,11), mutableListOf(
                    OpeningWeekdaysRow(
                            Weekdays(booleanArrayOf(true)),
                            TimeRange(0, 12*60)
                    ),
                    OpeningWeekdaysRow(
                            Weekdays(booleanArrayOf(false, true)),
                            TimeRange(12*60, 24*60)
                    )
            ))
    )
    private val openingHoursString = "Mo 00:00-12:00; Tu 12:00-24:00"

    @Test fun `apply yes answer`() {
        questType.verifyAnswer(HasFee, StringMapEntryAdd("fee", "yes"))
    }

    @Test fun `apply no answer`() {
        questType.verifyAnswer(HasNoFee, StringMapEntryAdd("fee", "no"))
    }

    @Test fun `apply only at hours answer`() {
        questType.verifyAnswer(
            HasFeeAtHours(openingHours),
            StringMapEntryAdd("fee", "no"),
            StringMapEntryAdd("fee:conditional", "yes @ ($openingHoursString)")
        )
    }

    @Test fun `apply yes except at hours answer`() {
        questType.verifyAnswer(
            HasFeeExceptAtHours(openingHours),
            StringMapEntryAdd("fee", "yes"),
            StringMapEntryAdd("fee:conditional", "no @ ($openingHoursString)")
        )
    }

}
