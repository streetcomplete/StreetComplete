package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit.*
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import kotlin.test.Test
import kotlin.test.assertEquals

class AddMaxWeightTest {

    private val questType = AddMaxWeight()

    @Test fun `apply metric weight answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "3.5")),
            questType.answerApplied(
                MaxWeight(listOf(MaxWeightType.MAX_WEIGHT), listOf(Weight(3.5, METRIC_TON)))
            )
        )
    }

    @Test fun `apply short tons answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "3.5 st")),
            questType.answerApplied(
                MaxWeight(listOf(MaxWeightType.MAX_WEIGHT), listOf(Weight(3.5, SHORT_TON)))
            )
        )
    }

    @Test fun `apply imperial pounds answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight", "500 lbs")),
            questType.answerApplied(MaxWeight(listOf(MaxWeightType.MAX_WEIGHT), listOf(Weight(500.0, POUND))))
        )
    }

    @Test fun `apply gvm weight answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweightrating", "3.5")),
            questType.answerApplied(MaxWeight(listOf(MaxWeightType.MAX_WEIGHT_RATING), listOf(Weight(3.5, METRIC_TON))))
        )
    }

    @Test fun `apply axle load answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxaxleload", "7.5")),
            questType.answerApplied(MaxWeight(listOf(MaxWeightType.MAX_AXLE_LOAD), listOf(Weight(7.5, METRIC_TON))))
        )
    }

    @Test fun `apply tandem axle load answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxbogieweight", "4")),
            questType.answerApplied(MaxWeight(listOf(MaxWeightType.MAX_TANDEM_AXLE_LOAD), listOf(Weight(4.0, METRIC_TON))))
        )
    }

    @Test fun `apply no maxweight sign answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxweight:signed", "no")),
            questType.answerApplied(MaxWeightAnswer.NoSign)
        )
    }
}
