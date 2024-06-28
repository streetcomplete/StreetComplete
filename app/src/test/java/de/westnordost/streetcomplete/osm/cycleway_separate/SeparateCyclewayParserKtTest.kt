package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import kotlin.test.*
import kotlin.test.Test

class SeparateCyclewayParserKtTest {

    @Test fun `parse no cycleway at all`() {
        assertEquals(null, parse())
        assertEquals(null, parse("highway" to "residential"))
        assertEquals(null, parse("highway" to "cycleway", "bicycle" to "no"))
        assertEquals(null, parse("highway" to "cycleway", "bicycle" to "dismount"))
    }

    @Test fun `parse path`() {
        assertEquals(PATH, parse("highway" to "path"))
        assertEquals(PATH, parse("highway" to "cycleway", "foot" to "yes", "bicycle" to "yes"))
        assertEquals(PATH, parse("highway" to "footway", "foot" to "yes", "bicycle" to "yes"))
        assertEquals(PATH, parse("highway" to "path", "foot" to "permissive", "bicycle" to "permissive"))
        assertEquals(PATH, parse("highway" to "path", "foot" to "customers", "bicycle" to "destination"))
        assertEquals(PATH, parse("highway" to "path", "bicycle" to "permissive"))
        assertEquals(PATH, parse("highway" to "path", "bicycle" to "private"))
        assertEquals(PATH, parse("highway" to "cycleway", "bicycle" to "yes"))
        assertEquals(PATH, parse("highway" to "cycleway", "bicycle" to "yes", "foot" to "no"))
    }

    @Test fun `parse not designated for cyclists`() {
        assertEquals(NON_DESIGNATED_ON_FOOTWAY, parse("highway" to "footway"))
    }

    @Test fun `parse no bicyclists allowed on path`() {
        assertEquals(NOT_ALLOWED, parse("highway" to "path", "bicycle" to "no", "bicycle:signed" to "yes"))
        assertEquals(NOT_ALLOWED, parse("highway" to "footway", "bicycle" to "no", "bicycle:signed" to "yes"))
    }

    @Test fun `parse bicyclists allowed on footway`() {
        assertEquals(ALLOWED_ON_FOOTWAY, parse("highway" to "footway", "bicycle" to "yes", "bicycle:signed" to "yes"))
        assertEquals(ALLOWED_ON_FOOTWAY, parse("highway" to "footway", "bicycle" to "destination", "bicycle:signed" to "yes"))
        assertEquals(ALLOWED_ON_FOOTWAY, parse("highway" to "path", "foot" to "designated", "bicycle" to "yes", "bicycle:signed" to "yes"))
        assertEquals(ALLOWED_ON_FOOTWAY, parse("highway" to "path", "foot" to "designated", "bicycle" to "permissive", "bicycle:signed" to "yes"))
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
        assertEquals(EXCLUSIVE, parse("highway" to "cycleway", "foot" to "use_sidepath"))

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

private fun parse(vararg pairs: Pair<String, String>) = parseSeparateCycleway(mapOf(*pairs))
