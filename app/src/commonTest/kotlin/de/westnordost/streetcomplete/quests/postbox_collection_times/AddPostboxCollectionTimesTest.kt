package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.Months
import de.westnordost.streetcomplete.osm.opening_hours.Times
import de.westnordost.streetcomplete.osm.opening_hours.Weekdays
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPostboxCollectionTimesTest {

    private val questType = AddPostboxCollectionTimes()

    @Test fun `apply no signed times answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("collection_times:signed", "no")),
            questType.answerApplied(NoCollectionTimesSign)
        )
    }

    @Test fun `apply times answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("collection_times", "10:00")),
            questType.answerApplied(CollectionTimes(HierarchicOpeningHours(listOf(
                Months(emptyList(), listOf(
                    Weekdays(listOf(), emptyList(),
                        Times(listOf(ClockTime(10)))
                    )
                ))
            ))))
        )
    }
}
