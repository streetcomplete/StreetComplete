package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.postbox_ref.AddPostboxRef
import de.westnordost.streetcomplete.quests.postbox_ref.NoRefVisible
import de.westnordost.streetcomplete.quests.postbox_ref.Ref
import org.junit.Test

class AddPostboxRefTest {

    private val questType = AddPostboxRef(mock())

    @Test fun `apply no ref answer`() {
        questType.verifyAnswer(
            NoRefVisible,
            StringMapEntryAdd("ref:signed", "no")
        )
    }

    @Test fun `apply ref answer`() {
        questType.verifyAnswer(
            Ref("12d"),
            StringMapEntryAdd("ref", "12d")
        )
    }

}
