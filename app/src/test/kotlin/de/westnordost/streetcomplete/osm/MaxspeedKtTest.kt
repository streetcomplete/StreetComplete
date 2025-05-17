package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaxspeedKtTest {
    @Test fun `get maxspeed`() {
        assertEquals(null, getMaxspeedInKmh(mapOf()))
        assertEquals(null, getMaxspeedInKmh(mapOf("unrelated" to "string")))
        assertEquals(null, getMaxspeedInKmh(mapOf("maxspeed" to "walk")))
        assertEquals(10f, getMaxspeedInKmh(mapOf("maxspeed" to "10")))
        assertEquals(160.9344f, getMaxspeedInKmh(mapOf("maxspeed" to "100 mph")))
    }

    @Test fun `guess maxspeed`() {
        assertEquals(null, guessMaxspeedInKmh(mapOf()))
        assertEquals(null, guessMaxspeedInKmh(mapOf("unrelated" to "string")))
        assertEquals(5f, guessMaxspeedInKmh(mapOf("maxspeed" to "walk")))

        assertEquals(50f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:urban")))
        assertEquals(50f, guessMaxspeedInKmh(mapOf("maxspeed" to "DE:urban")))
        assertEquals(50f, guessMaxspeedInKmh(mapOf("source:maxspeed" to "DE:urban")))
        assertEquals(50f, guessMaxspeedInKmh(mapOf("zone:traffic" to "DE:urban")))

        assertEquals(70f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:rural")))
        assertEquals(120f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:motorway")))
        assertEquals(30f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:zone30")))
        assertEquals(10f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:zone:10")))
        assertEquals(20f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:20")))
    }

    @Test fun `guess maxspeed mph zone`() {
        val countryInfos: CountryInfos = mock()
        val countryInfo: CountryInfo = mock()
        on(countryInfo.speedUnits).thenReturn(listOf(SpeedMeasurementUnit.MILES_PER_HOUR))
        on(countryInfos.get(any())).thenReturn(countryInfo)

        assertEquals(32.18688f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:zone:20"), countryInfos)!!, 0.1f)
        assertEquals(48.28032f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:zone30"), countryInfos)!!, 0.1f)
        assertEquals(16.09344f, guessMaxspeedInKmh(mapOf("maxspeed:type" to "DE:10"), countryInfos)!!, 0.1f)
    }

    @Test fun isImplicitMaxSpeed() {
        assertTrue(matchesImplicitMaxSpeed())
        assertTrue(matchesImplicitMaxSpeed("maxspeed" to "implicit"))
        assertTrue(matchesImplicitMaxSpeed("maxspeed" to "DE:living_street"))
        assertTrue(matchesImplicitMaxSpeed("maxspeed" to "BE-BRU:motorway"))
        assertFalse(matchesImplicitMaxSpeed("maxspeed" to "30"))
        assertFalse(matchesImplicitMaxSpeed("maxspeed" to "walk"))

        assertTrue(matchesImplicitMaxSpeed("maxspeed" to "30", "maxspeed:type" to "DE:zone30"))
        assertTrue(matchesImplicitMaxSpeed("source:maxspeed" to "DE:zone30"))
        assertFalse(matchesImplicitMaxSpeed("maxspeed" to "30", "source:maxspeed" to "sign"))
    }

    @Test fun isInSlowZone() {
        // different syntax for values
        assertTrue(matchesInSlowZone("maxspeed" to "DE:zone30"))
        assertTrue(matchesInSlowZone("maxspeed" to "DE:zone:30"))
        assertTrue(matchesInSlowZone("maxspeed" to "DE:30"))
        // different keys
        for (k in MAXSPEED_TYPE_KEYS + "maxspeed") {
            assertTrue(matchesInSlowZone(k to "DE:zone30"))
        }
        // different values
        for (i in 1..30) {
            assertTrue(matchesInSlowZone("maxspeed:type" to "DE:zone$i"))
        }
        // negative checks
        assertFalse(matchesInSlowZone("maxspeed:type" to "DE:zone31"))
        assertFalse(matchesInSlowZone("maxspeed:type" to "zone30"))
        assertFalse(matchesInSlowZone("maxspeed:type" to "DE:urban"))
    }

    private fun matchesImplicitMaxSpeed(vararg tags: Pair<String, String>): Boolean =
        "nodes with $isImplicitMaxSpeed"
            .toElementFilterExpression()
            .matches(node(tags = mapOf(*tags)))

    private fun matchesInSlowZone(vararg tags: Pair<String, String>): Boolean =
        "nodes with $isInSlowZone"
            .toElementFilterExpression()
            .matches(node(tags = mapOf(*tags)))
}
