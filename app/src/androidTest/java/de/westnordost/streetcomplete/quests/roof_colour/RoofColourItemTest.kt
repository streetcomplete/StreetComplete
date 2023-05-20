package de.westnordost.streetcomplete.quests.roof_colour

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test

class RoofColourItemTest {
    @Test
    fun parsableAsColor() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        RoofColour.values().mapNotNull {
            assertNotNull(
                it.asItem(context)
            )
        }
    }
}
