package de.westnordost.streetcomplete.screens.main.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MainMapStyleTransitionTest {

    @Test fun defaultMotionScalePreservesLegacyDuration() {
        assertEquals(300.milliseconds, mainMapStyleTransition(1f).duration)
    }

    @Test fun systemMotionScaleChangesTransitionDuration() {
        assertEquals(150.milliseconds, mainMapStyleTransition(0.5f).duration)
        assertEquals(600.milliseconds, mainMapStyleTransition(2f).duration)
    }

    @Test fun disabledSystemMotionRemovesTransitionDuration() {
        assertEquals(Duration.ZERO, mainMapStyleTransition(0f).duration)
    }
}
