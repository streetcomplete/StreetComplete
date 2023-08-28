package de.westnordost.streetcomplete.quests.roof_colour

import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape
import kotlin.test.Test
import kotlin.test.assertNotNull

class RoofColourItemTest {
    @Test
    fun parsableAsColorIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        RoofShape.values().map { shape ->
            RoofColour.values().map {
                assertNotNull(
                    it.asItem(context, shape)
                )
            }
        }
    }

    @Test
    fun parsableAsColorIconNoShape() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        RoofColour.values().map {
            assertNotNull(
                it.asItem(context, null)
            )
        }
    }
}
