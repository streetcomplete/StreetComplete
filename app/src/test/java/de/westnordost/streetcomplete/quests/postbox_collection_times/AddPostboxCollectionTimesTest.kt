package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.TwentyFourSeven
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
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
            setOf(StringMapEntryAdd("collection_times", "24/7")),
            questType.answerApplied(CollectionTimes(OpeningHours(listOf(Rule(TwentyFourSeven)))))
        )
    }
}
