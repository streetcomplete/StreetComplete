package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.HAS_SEPARATE_SIDEWALK
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.IS_LIVING_STREET
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.NO
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.YES
import org.junit.Test

class AddProhibitedForPedestriansTest {

    private val questType = AddProhibitedForPedestrians()

    @Test fun `apply yes answer`() {
        questType.verifyAnswer(YES, StringMapEntryAdd("foot", "no"))
    }

    @Test fun `apply no answer`() {
        questType.verifyAnswer(NO, StringMapEntryAdd("foot", "yes"))
    }

    @Test fun `apply separate sidewalk answer`() {
        questType.verifyAnswer(
            mapOf("sidewalk" to "no"),
            HAS_SEPARATE_SIDEWALK,
            StringMapEntryModify("sidewalk", "no", "separate")
        )
    }

    @Test fun `apply living street answer`() {
        questType.verifyAnswer(
            mapOf("highway" to "residential"),
            IS_LIVING_STREET,
            StringMapEntryModify("highway", "residential", "living_street")
        )
    }
}
