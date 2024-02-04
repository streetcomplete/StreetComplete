package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
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

    @Test fun `apply separate sidewalk answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk:both", "separate")),
            questType.answerApplied(HAS_SEPARATE_SIDEWALK)
        )
    }

    @Test fun `remove wrong sidewalk tagging`() {
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk:both", "yes", "separate"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            questType.answerAppliedTo(HAS_SEPARATE_SIDEWALK, mapOf(
                "sidewalk" to "both",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
                "sidewalk:both" to "yes"
            ))
        )
    }

    @Test fun `apply living street answer`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "residential", "living_street")),
            questType.answerAppliedTo(IS_LIVING_STREET, mapOf("highway" to "residential"))
        )
    }
}
