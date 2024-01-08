package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.verifyAnswer
import kotlin.test.Test

class AddMaxWeightTest {

    private val questType = AddMaxWeight()

    @Test fun `apply metric weight answer`() {
        questType.verifyAnswer(
            MaxWeight(MaxWeightSign.MAX_WEIGHT, MetricTons(3.5)),
            StringMapEntryAdd("maxweight", "3.5")
        )
    }

    @Test fun `apply short tons answer`() {
        questType.verifyAnswer(
            MaxWeight(MaxWeightSign.MAX_WEIGHT, ShortTons(3.5)),
            StringMapEntryAdd("maxweight", "3.5 st")
        )
    }

    @Test fun `apply imperial pounds answer`() {
        questType.verifyAnswer(
            MaxWeight(MaxWeightSign.MAX_WEIGHT, ImperialPounds(500)),
            StringMapEntryAdd("maxweight", "500 lbs")
        )
    }

    @Test fun `apply gvm weight answer`() {
        questType.verifyAnswer(
            MaxWeight(MaxWeightSign.MAX_GROSS_VEHICLE_MASS, MetricTons(3.5)),
            StringMapEntryAdd("maxweightrating", "3.5")
        )
    }

    @Test fun `apply axle load answer`() {
        questType.verifyAnswer(
            MaxWeight(MaxWeightSign.MAX_AXLE_LOAD, MetricTons(7.5)),
            StringMapEntryAdd("maxaxleload", "7.5")
        )
    }

    @Test fun `apply tandem axle load answer`() {
        questType.verifyAnswer(
            MaxWeight(MaxWeightSign.MAX_TANDEM_AXLE_LOAD, MetricTons(4.0)),
            StringMapEntryAdd("maxbogieweight", "4")
        )
    }

    @Test fun `apply default height answer`() {
        questType.verifyAnswer(
            NoMaxWeightSign,
            StringMapEntryAdd("maxweight:signed", "no")
        )
    }
}
