package de.westnordost.streetcomplete.screens.main.map.layers

import kotlin.test.Test
import kotlin.test.assertEquals

class FocusedGeometryStyleTest {

    @Test fun preservesLegacyBreathingRanges() {
        val smallest = focusedGeometryStyle(0.75f)
        val largest = focusedGeometryStyle(0.25f)

        assertEquals(FocusedGeometryStyle(0.65f, 7.5f, 9f), smallest)
        assertEquals(FocusedGeometryStyle(0.15f, 17.5f, 21f), largest)
    }
}
