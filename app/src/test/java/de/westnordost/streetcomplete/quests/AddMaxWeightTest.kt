package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.max_weight.*
import org.junit.Test

class AddMaxWeightTest {

    private val questType = AddMaxWeight(mock())

    @Test fun `apply metric weight answer`() {
        questType.verifyAnswer(
            MaxWeight(MetricTons(3.5)),
            StringMapEntryAdd("maxweight","3.5")
        )
    }

    @Test fun `apply short tons answer`() {
        questType.verifyAnswer(
            MaxWeight(ShortTons(3.5)),
            StringMapEntryAdd("maxweight","3.5 st")
        )
    }

    @Test fun `apply imperial pounds answer`() {
        questType.verifyAnswer(
            MaxWeight(ImperialPounds(500)),
            StringMapEntryAdd("maxweight","500 lbs")
        )
    }

    @Test fun `apply default height answer`() {
        questType.verifyAnswer(
            NoMaxWeightSign,
            StringMapEntryAdd("maxweight:signed","no")
        )
    }
}
