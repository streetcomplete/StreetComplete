package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test
import java.util.*

class AddStepsRampTest {

    private val questType = AddStepsRamp(mock(), mock())

    @Test fun `apply bicycle ramp answer`() {
        questType.verifyAnswer(
            StepsRampAnswer(
                bicycleRamp = true,
                strollerRamp = false,
                wheelchairRamp = false
            ),
            StringMapEntryAdd("ramp", "yes"),
            StringMapEntryAdd("ramp:bicycle", "yes")
        )
    }

    @Test fun `apply stroller ramp answer`() {
        questType.verifyAnswer(
            mapOf(
                "ramp" to "no"
            ),
            StepsRampAnswer(
                bicycleRamp = false,
                strollerRamp = true,
                wheelchairRamp = false
            ),
            StringMapEntryModify("ramp", "no", "yes"),
            StringMapEntryAdd("ramp:stroller", "yes")
        )
    }

    @Test fun `apply wheelchair answer`() {
        questType.verifyAnswer(
            mapOf(
                "ramp" to "yes"
            ),
            StepsRampAnswer(
                bicycleRamp = false,
                strollerRamp = false,
                wheelchairRamp = true
            ),
            StringMapEntryAdd("ramp:wheelchair", "yes"),
            StringMapEntryAdd("check_date:ramp", Date().toCheckDateString()),
        )
    }

    @Test fun `apply multiple ramps answer`() {
        questType.verifyAnswer(
            StepsRampAnswer(
                bicycleRamp = true,
                strollerRamp = true,
                wheelchairRamp = true
            ),
            StringMapEntryAdd("ramp", "yes"),
            StringMapEntryAdd("ramp:stroller", "yes"),
            StringMapEntryAdd("ramp:bicycle", "yes"),
            StringMapEntryAdd("ramp:wheelchair", "yes"),
        )
    }

    @Test fun `update ramps answer to yes`() {
        questType.verifyAnswer(
            mapOf(
                "ramp:bicycle" to "yes",
                "ramp:stroller" to "no",
                "ramp:wheelchair" to "automatic",
                "ramp" to "yes",
            ),
            StepsRampAnswer(
                bicycleRamp = true,
                strollerRamp = true,
                wheelchairRamp = true
            ),
            StringMapEntryModify("ramp:bicycle", "yes", "yes"),
            StringMapEntryModify("ramp:stroller", "no", "yes"),
            StringMapEntryModify("ramp:wheelchair", "automatic", "yes"),
            StringMapEntryAdd("check_date:ramp", Date().toCheckDateString()),
        )
    }

    @Test fun `update ramps answer to no`() {
        questType.verifyAnswer(
            mapOf(
                "ramp:bicycle" to "yes",
                "ramp:stroller" to "no",
                "ramp:wheelchair" to "separate",
                "ramp" to "yes",
            ),
            StepsRampAnswer(
                bicycleRamp = false,
                strollerRamp = false,
                wheelchairRamp = false
            ),
            StringMapEntryModify("ramp", "yes","no"),
            StringMapEntryDelete("ramp:bicycle", "yes")
        )
    }

    @Test fun `ramp value is not set to no on updating all ramps to no if there is an unsupported rampnot tagged no`() {
        questType.verifyAnswer(
            mapOf(
                "ramp:luggage" to "automatic",
                "ramp:bicycle" to "yes",
                "ramp" to "yes",
            ),
            StepsRampAnswer(
                bicycleRamp = false,
                strollerRamp = false,
                wheelchairRamp = false
            ),
            StringMapEntryAdd("check_date:ramp", Date().toCheckDateString()),
            StringMapEntryDelete("ramp:bicycle", "yes")
        )
    }
}