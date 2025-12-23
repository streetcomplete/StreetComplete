package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit.*
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.*
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddMaxWeightTest {

    private val questType = AddMaxWeight()

    @Test fun `apply metric weight answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "3.5")),
            questType.answerApplied(listOf(MaxWeight(MAX_WEIGHT, Weight(3.5, METRIC_TON))))
        )
    }

    @Test fun `apply short tons answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "3.5 st")),
            questType.answerApplied(listOf(MaxWeight(MAX_WEIGHT, Weight(3.5, SHORT_TON))))
        )
    }

    @Test fun `apply imperial pounds answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "500 lbs")),
            questType.answerApplied(listOf(MaxWeight(MAX_WEIGHT, Weight(500.0, POUND))))
        )
    }

    @Test fun `apply gvm weight answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweightrating", "3.5")),
            questType.answerApplied(listOf(MaxWeight(MAX_WEIGHT_RATING, Weight(3.5, METRIC_TON))))
        )
    }

    @Test fun `apply axle load answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxaxleload", "7.5")),
            questType.answerApplied(listOf(MaxWeight(MAX_AXLE_LOAD, Weight(7.5, METRIC_TON))))
        )
    }

    @Test fun `apply tandem axle load answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxbogieweight", "4")),
            questType.answerApplied(listOf(MaxWeight(MAX_TANDEM_AXLE_LOAD, Weight(4.0, METRIC_TON))))
        )
    }

    @Test fun `apply multiple signs answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("maxweight", "3.5"),
                StringMapEntryAdd("maxaxleload", "7.5"),
                StringMapEntryAdd("maxbogieweight", "4"),
            ),
            questType.answerApplied(listOf(
                MaxWeight(MAX_TANDEM_AXLE_LOAD, Weight(4.0, METRIC_TON)),
                MaxWeight(MAX_AXLE_LOAD, Weight(7.5, METRIC_TON)),
                MaxWeight(MAX_WEIGHT, Weight(3.5, METRIC_TON)),
            ))
        )
    }

    @Test fun `apply no maxweight sign answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight:signed", "no")),
            questType.answerApplied(emptyList())
        )
    }
}
