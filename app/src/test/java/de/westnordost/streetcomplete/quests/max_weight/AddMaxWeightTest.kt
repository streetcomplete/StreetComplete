package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddMaxWeightTest {

    private val questType = AddMaxWeight()

    @Test fun `apply metric weight answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "3.5")),
            questType.answerApplied(MaxWeight(MaxWeightSign.MAX_WEIGHT, MetricTons(3.5)))
        )
    }

    @Test fun `apply short tons answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "3.5 st")),
            questType.answerApplied(MaxWeight(MaxWeightSign.MAX_WEIGHT, ShortTons(3.5)))
        )
    }

    @Test fun `apply imperial pounds answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "500 lbs")),
            questType.answerApplied(MaxWeight(MaxWeightSign.MAX_WEIGHT, ImperialPounds(500)))
        )
    }

    @Test fun `apply gvm weight answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweightrating", "3.5")),
            questType.answerApplied(MaxWeight(MaxWeightSign.MAX_GROSS_VEHICLE_MASS, MetricTons(3.5)))
        )
    }

    @Test fun `apply axle load answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxaxleload", "7.5")),
            questType.answerApplied(MaxWeight(MaxWeightSign.MAX_AXLE_LOAD, MetricTons(7.5)))
        )
    }

    @Test fun `apply tandem axle load answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxbogieweight", "4")),
            questType.answerApplied(MaxWeight(MaxWeightSign.MAX_TANDEM_AXLE_LOAD, MetricTons(4.0)))
        )
    }

    @Test fun `apply no maxweight sign answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight:signed", "no")),
            questType.answerApplied(NoMaxWeightSign)
        )
    }
}
