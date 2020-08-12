package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sidewalk.SeparatelyMapped
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkAnswer
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkSides
import org.junit.Test

class AddSidewalkTest {

    private val questType = AddSidewalk(mock())

    @Test fun `apply no sidewalk answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = false, right = false),
            StringMapEntryAdd("sidewalk", "none")
        )
    }

    @Test fun `apply sidewalk left answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = true, right = false),
            StringMapEntryAdd("sidewalk", "left")
        )
    }

    @Test fun `apply sidewalk right answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = false, right = true),
            StringMapEntryAdd("sidewalk", "right")
        )
    }

    @Test fun `apply sidewalk on both sides answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = true, right = true),
            StringMapEntryAdd("sidewalk", "both")
        )
    }

    @Test fun `apply separate sidewalk answer`() {
        questType.verifyAnswer(
            SeparatelyMapped,
            StringMapEntryAdd("sidewalk", "separate")
        )
    }
}
