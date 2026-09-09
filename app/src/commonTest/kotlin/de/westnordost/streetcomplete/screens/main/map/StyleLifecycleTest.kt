package de.westnordost.streetcomplete.screens.main.map

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StyleLifecycleTest {
    @Test fun onlyKnownStyleGenerationRacesAreRecoverable() {
        assertTrue(IllegalStateException("No ready loaded style").isStyleHandleRace())
        assertTrue(
            IllegalStateException(
                "Style operation belongs to a stale loaded-style identity"
            ).isStyleHandleRace()
        )
        assertTrue(
            IllegalStateException(
                "Style operation belongs to a stale or unready loaded-style identity"
            ).isStyleHandleRace()
        )
        assertFalse(IllegalStateException("Could not parse GeoJSON").isStyleHandleRace())
    }
}
