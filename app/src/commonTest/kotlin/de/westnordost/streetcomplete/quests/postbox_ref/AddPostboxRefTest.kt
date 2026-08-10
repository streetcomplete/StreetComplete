package de.westnordost.streetcomplete.quests.postbox_ref

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPostboxRefTest {

    private val questType = AddPostboxRef()

    @Test fun `apply no ref answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("ref:signed", "no")),
            questType.answerApplied(NoVisiblePostboxRef)
        )
    }

    @Test fun `apply ref answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("ref", "12d")),
            questType.answerApplied(PostboxRef("12d"))
        )
    }
}
