package de.westnordost.streetcomplete.quests.building_colour

import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import kotlin.test.assertNotNull

class BuildingColourItemTest {
    @Test
    fun parsableAsColorIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        BuildingColour.values().map {
            assertNotNull(
                it.asItem(context)
            )
        }
    }
}
