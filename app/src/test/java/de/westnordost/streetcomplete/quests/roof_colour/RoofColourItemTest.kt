package de.westnordost.streetcomplete.quests.roof_colour

import org.junit.Assert.assertNotNull
import org.junit.Test

class RoofColourItemTest {
    @Test
    fun `parsable as Color`() {
        RoofColour.values().mapNotNull {
            assertNotNull(
                it.asItem()
            )
        }
    }
}
