package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddStepsRampTest {

    private val questType = AddStepsRamp()

    @Test fun `apply bicycle ramp answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("ramp", "yes"),
                StringMapEntryAdd("ramp:bicycle", "yes")
            ),
            questType.answerApplied(
                StepsRampAnswer(
                    bicycleRamp = true,
                    strollerRamp = false,
                    wheelchairRamp = WheelchairRampStatus.NO
                )
            )
        )
    }

    @Test fun `apply stroller ramp answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("ramp", "no", "yes"),
                StringMapEntryAdd("ramp:stroller", "yes")
            ),
            questType.answerAppliedTo(
                StepsRampAnswer(
                    bicycleRamp = false,
                    strollerRamp = true,
                    wheelchairRamp = WheelchairRampStatus.NO
                ),
                mapOf("ramp" to "no")
            )
        )
    }

    @Test fun `apply wheelchair answer`() {
        assertEquals(
            setOf(
                StringMapEntryModify("ramp", "yes", "yes"),
                StringMapEntryAdd("ramp:wheelchair", "yes"),
                StringMapEntryAdd("check_date:ramp", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(
                StepsRampAnswer(
                    bicycleRamp = false,
                    strollerRamp = false,
                    wheelchairRamp = WheelchairRampStatus.YES
                ),
                mapOf("ramp" to "yes")
            )
        )
    }

    @Test fun `apply multiple ramps answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("ramp", "yes"),
                StringMapEntryAdd("ramp:stroller", "yes"),
                StringMapEntryAdd("ramp:bicycle", "yes"),
                StringMapEntryAdd("ramp:wheelchair", "yes"),
            ),
            questType.answerApplied(
                StepsRampAnswer(
                    bicycleRamp = true,
                    strollerRamp = true,
                    wheelchairRamp = WheelchairRampStatus.YES
                )
            )
        )
    }

    @Test fun `update ramps answer to yes`() {
        assertEquals(
            setOf(
                StringMapEntryModify("ramp", "yes", "yes"),
                StringMapEntryModify("ramp:bicycle", "yes", "yes"),
                StringMapEntryModify("ramp:stroller", "no", "yes"),
                StringMapEntryModify("ramp:wheelchair", "automatic", "yes"),
                StringMapEntryAdd("check_date:ramp", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(
                StepsRampAnswer(
                    bicycleRamp = true,
                    strollerRamp = true,
                    wheelchairRamp = WheelchairRampStatus.YES
                ),
                mapOf(
                    "ramp:bicycle" to "yes",
                    "ramp:stroller" to "no",
                    "ramp:wheelchair" to "automatic",
                    "ramp" to "yes",
                )
            )
        )
    }

    @Test fun `update ramps answer to no`() {
        assertEquals(
            setOf(
                StringMapEntryModify("ramp", "yes", "no"),
                StringMapEntryDelete("ramp:bicycle", "yes"),
                StringMapEntryDelete("ramp:wheelchair", "separate"),
            ),
            questType.answerAppliedTo(
                StepsRampAnswer(
                    bicycleRamp = false,
                    strollerRamp = false,
                    wheelchairRamp = WheelchairRampStatus.NO
                ),
                mapOf(
                    "ramp:bicycle" to "yes",
                    "ramp:stroller" to "no",
                    "ramp:wheelchair" to "separate",
                    "ramp" to "yes",
                )
            )
        )
    }

    @Test fun `ramp value is not set to no on updating all ramps to no if there is an unsupported ramp not tagged no`() {
        assertEquals(
            setOf(
                StringMapEntryModify("ramp", "yes", "yes"),
                StringMapEntryAdd("check_date:ramp", nowAsCheckDateString()),
                StringMapEntryAdd("ramp:bicycle", "no"),
                StringMapEntryAdd("ramp:stroller", "no"),
                StringMapEntryAdd("ramp:wheelchair", "no"),
            ),
            questType.answerAppliedTo(
                StepsRampAnswer(
                    bicycleRamp = false,
                    strollerRamp = false,
                    wheelchairRamp = WheelchairRampStatus.NO
                ),
                mapOf(
                    "ramp:luggage" to "automatic",
                    "ramp" to "yes",
                )
            )
        )
    }

    @Test fun `apply wheelchair separate ramp answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("ramp", "separate"),
                StringMapEntryAdd("ramp:wheelchair", "separate")
            ),
            questType.answerApplied(
                StepsRampAnswer(
                    bicycleRamp = false,
                    strollerRamp = false,
                    wheelchairRamp = WheelchairRampStatus.SEPARATE
                )
            )
        )
    }

    @Test fun `apply wheelchair separate answer deletes previous ramp values`() {
        questType.answerAppliedTo(
            StepsRampAnswer(
                bicycleRamp = false,
                strollerRamp = false,
                wheelchairRamp = WheelchairRampStatus.SEPARATE
            ),
            mapOf(
                "ramp" to "yes",
                "ramp:bicycle" to "yes",
                "ramp:stroller" to "yes",
            )
        )

        setOf(
            StringMapEntryModify("ramp", "yes", "separate"),
            StringMapEntryAdd("ramp:wheelchair", "separate"),
            StringMapEntryDelete("ramp:bicycle", "yes"),
            StringMapEntryDelete("ramp:stroller", "yes")
        )
    }

    @Test fun `apply wheelchair separate answer with other ramps`() {
        questType.answerAppliedTo(
            StepsRampAnswer(
                bicycleRamp = true,
                strollerRamp = true,
                wheelchairRamp = WheelchairRampStatus.SEPARATE
            ),
            mapOf(
                "ramp" to "no",
                "ramp:stroller" to "yes",
            )
        )

        setOf(
            StringMapEntryModify("ramp", "no", "yes"),
            StringMapEntryAdd("ramp:wheelchair", "separate"),
            StringMapEntryAdd("ramp:bicycle", "yes"),
            StringMapEntryModify("ramp:stroller", "yes", "yes")
        )
    }
}
