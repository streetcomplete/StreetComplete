package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import org.junit.Assert.*
import org.junit.Test

class SeparateCyclewayParserKtTest {

    @Test fun `parse no cycleway at all`() {
        assertEquals(null, parse())
        assertEquals(null, parse("highway" to "residential"))
    }

    @Test fun `parse no bicyclists allowed on path`() {
        assertEquals(NONE, parse("highway" to "path"))
        assertEquals(NONE, parse("highway" to "footway"))

        assertEquals(NONE, parse("highway" to "path", "bicycle" to "no"))
        assertEquals(NONE, parse("highway" to "footway", "bicycle" to "no"))
        assertEquals(NONE, parse("highway" to "cycleway", "bicycle" to "no"))
    }

    @Test fun `parse bicyclists allowed on path`() {
        assertEquals(ALLOWED, parse("highway" to "path", "bicycle" to "yes"))
        assertEquals(ALLOWED, parse("highway" to "footway", "bicycle" to "yes"))
        assertEquals(ALLOWED, parse("highway" to "cycleway", "bicycle" to "yes"))
    }

    @Test fun `parse cyclists on non-segregated path`() {
        assertEquals(NON_SEGREGATED, parse("highway" to "path", "bicycle" to "designated", "segregated" to "no"))
        assertEquals(NON_SEGREGATED, parse("highway" to "footway", "bicycle" to "designated", "segregated" to "no"))
        assertEquals(NON_SEGREGATED, parse("highway" to "cycleway", "segregated" to "no", "foot" to "yes"))
        assertEquals(NON_SEGREGATED, parse("highway" to "cycleway", "segregated" to "no", "foot" to "designated"))
        assertEquals(NON_SEGREGATED, parse("highway" to "cycleway", "bicycle" to "designated", "segregated" to "no", "foot" to "yes"))
    }

    @Test fun `parse cyclists on segregated path`() {
        assertEquals(SEGREGATED, parse("highway" to "path", "bicycle" to "designated", "segregated" to "yes"))
        assertEquals(SEGREGATED, parse("highway" to "footway", "bicycle" to "designated", "segregated" to "yes"))

        assertEquals(SEGREGATED, parse("highway" to "cycleway", "bicycle" to "designated", "segregated" to "yes", "foot" to "yes"))
        assertEquals(SEGREGATED, parse("highway" to "cycleway", "segregated" to "yes", "foot" to "yes"))
        assertEquals(SEGREGATED, parse("highway" to "cycleway", "segregated" to "yes", "foot" to "designated"))
    }

    @Test fun `parse cyclists on exclusive path`() {
        assertEquals(EXCLUSIVE, parse("highway" to "path", "bicycle" to "designated", "foot" to "no"))
        assertEquals(EXCLUSIVE, parse("highway" to "footway", "bicycle" to "designated", "foot" to "no"))
        assertEquals(EXCLUSIVE, parse("highway" to "cycleway"))
        assertEquals(EXCLUSIVE, parse("highway" to "cycleway", "foot" to "no"))

        assertEquals(EXCLUSIVE, parse("highway" to "cycleway", "sidewalk" to "separate"))
        assertEquals(EXCLUSIVE, parse("highway" to "cycleway", "sidewalk" to "no"))
    }

    @Test fun `parse cycleway with sidewalk`() {
        assertEquals(EXCLUSIVE_WITH_SIDEWALK, parse("highway" to "path", "bicycle" to "designated", "sidewalk" to "left"))
        assertEquals(EXCLUSIVE_WITH_SIDEWALK, parse("highway" to "footway", "bicycle" to "designated", "sidewalk" to "right"))

        assertEquals(EXCLUSIVE_WITH_SIDEWALK, parse("highway" to "cycleway", "bicycle" to "designated", "sidewalk:left" to "yes"))
        assertEquals(EXCLUSIVE_WITH_SIDEWALK, parse("highway" to "cycleway", "sidewalk:both" to "yes"))
        assertEquals(EXCLUSIVE_WITH_SIDEWALK, parse("highway" to "cycleway", "sidewalk:right" to "yes"))

        assertEquals(EXCLUSIVE_WITH_SIDEWALK, parse("highway" to "cycleway", "sidewalk" to "yes"))
    }
}

private fun parse(vararg pairs: Pair<String, String>) = createSeparateCycleway(mapOf(*pairs))
