package de.westnordost.streetcomplete.quests.postbox_ref

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddPostboxRefTest {

    private val questType = AddPostboxRef()

    @Test fun `apply no ref answer`() {
        questType.verifyAnswer(
            NoVisiblePostboxRef,
            StringMapEntryAdd("ref:signed", "no")
        )
    }

    @Test fun `apply ref answer`() {
        questType.verifyAnswer(
            PostboxRef("12d"),
            StringMapEntryAdd("ref", "12d")
        )
    }
}
