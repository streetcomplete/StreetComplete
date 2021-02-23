package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import de.westnordost.streetcomplete.quests.postbox_collection_times.*
import org.junit.Test

class AddPostboxCollectionTimesTest {

    private val questType = AddPostboxCollectionTimes()

    @Test fun `apply no signed times answer`() {
        questType.verifyAnswer(
            NoCollectionTimesSign,
            StringMapEntryAdd("collection_times:signed", "no")
        )
    }

    @Test fun `apply collection times answer`() {
        questType.verifyAnswer(
            CollectionTimes(listOf(
                WeekdaysTimes(Weekdays(booleanArrayOf(true)), mutableListOf(60)),
                WeekdaysTimes(Weekdays(booleanArrayOf(false, true)), mutableListOf(120))
            )),
            // here ; would be fine as well instead of ,
            // see https://github.com/streetcomplete/StreetComplete/pull/2604#issuecomment-783823068
            StringMapEntryAdd("collection_times", "Mo 01:00, Tu 02:00")
        )
    }

    // for non-overlapping day ranges it does not matter whether
    // comma or semicolon is used - but for overlapping ones it matters
    // see https://github.com/streetcomplete/StreetComplete/pull/2604#issuecomment-783823068
    @Test fun `require comma where this matters`() {
        questType.verifyAnswer(
            CollectionTimes(listOf(
                WeekdaysTimes(Weekdays(booleanArrayOf(true, true, true)), mutableListOf(60)),
                WeekdaysTimes(Weekdays(booleanArrayOf(false, true)), mutableListOf(120))
            )),
            StringMapEntryAdd("collection_times", "Mo-We 01:00, Tu 02:00")
        )
    }

    @Test fun `require comma where this matters and conflict is between nonadjacent ranges`() {
        questType.verifyAnswer(
            CollectionTimes(listOf(
                WeekdaysTimes(Weekdays(booleanArrayOf(true, true, true)), mutableListOf(60)),
                WeekdaysTimes(Weekdays(booleanArrayOf(false, false, false, true, true, true, true)), mutableListOf(120)),
                WeekdaysTimes(Weekdays(booleanArrayOf(true)), mutableListOf(180))
            )),
            StringMapEntryAdd("collection_times", "Mo-We 01:00, Th-Su 02:00, Mo 03:00")
        )
    }
}
