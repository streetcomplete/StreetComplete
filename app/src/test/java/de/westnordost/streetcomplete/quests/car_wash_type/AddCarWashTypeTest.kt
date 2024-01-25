package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.AUTOMATED
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SELF_SERVICE
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SERVICE
import kotlin.test.Test
import kotlin.test.assertEquals

class AddCarWashTypeTest {

    private val questType = AddCarWashType()

    @Test fun `only self service`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "only"),
                StringMapEntryAdd("automated", "no")
            ),
            questType.answerApplied(listOf(SELF_SERVICE))
        )
    }

    @Test fun `only automated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "no"),
                StringMapEntryAdd("automated", "yes")
            ),
            questType.answerApplied(listOf(AUTOMATED))
        )
    }

    @Test fun `only staff`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "no"),
                StringMapEntryAdd("automated", "no")
            ),
            questType.answerApplied(listOf(SERVICE))
        )
    }

    @Test fun `automated and self service`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "yes"),
                StringMapEntryAdd("automated", "yes")
            ),
            questType.answerApplied(listOf(AUTOMATED, SELF_SERVICE))
        )
    }

    @Test fun `automated and staff cleans car is tagged the same as automated only`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "no"),
                StringMapEntryAdd("automated", "yes")
            ),
            questType.answerApplied(listOf(AUTOMATED, SERVICE))
        )
    }

    @Test fun `self service and staff cleans car`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "yes"),
                StringMapEntryAdd("automated", "no")
            ),
            questType.answerApplied(listOf(SELF_SERVICE, SERVICE))
        )
    }

    @Test fun `automated, self service and staff cleans car is tagged the same way as automated and self service`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("self_service", "yes"),
                StringMapEntryAdd("automated", "yes")
            ),
            questType.answerApplied(listOf(AUTOMATED, SELF_SERVICE, SERVICE))
        )
    }
}
