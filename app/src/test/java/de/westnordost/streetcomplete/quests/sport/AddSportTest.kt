package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.sport.Sport.SOCCER
import de.westnordost.streetcomplete.quests.sport.Sport.TENNIS
import kotlin.test.Test
import kotlin.test.assertEquals

class AddSportTest {

    private val questType = AddSport()

    @Test fun `replace previous sport answer`() {
        assertEquals(
            setOf(StringMapEntryModify("sport", "football", "soccer")),
            questType.answerAppliedTo(listOf(SOCCER), mapOf("sport" to "football"))
        )
    }

    @Test fun `apply sport answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("sport", "soccer")),
            questType.answerApplied(listOf(SOCCER))
        )
    }

    @Test fun `apply multiple sports answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("sport", "soccer;tennis")),
            questType.answerApplied(listOf(SOCCER, TENNIS))
        )
    }
}
