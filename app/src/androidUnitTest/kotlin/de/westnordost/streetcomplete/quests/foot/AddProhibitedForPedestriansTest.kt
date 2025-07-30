package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AddProhibitedForPedestriansTest {

    private val questType = AddProhibitedForPedestrians()

    @Test fun `apply yes answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("foot", "no")),
            questType.answerApplied(YES)
        )
    }

    @Test fun `apply no answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("foot", "yes")),
            questType.answerApplied(NO)
        )
    }

    @Test fun `apply actually sidewalk answer clears all sidewalk tagging`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("sidewalk:both", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            questType.answerAppliedTo(ACTUALLY_HAS_SIDEWALK, mapOf(
                "sidewalk" to "both",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
                "sidewalk:both" to "yes"
            ))
        )
    }
}
