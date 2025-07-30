package de.westnordost.streetcomplete.osm.maxspeed

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaxSpeedUtilsTest {

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
        for (k in MAX_SPEED_TYPE_KEYS + "maxspeed") {
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
        "nodes with $FILTER_IS_IMPLICIT_MAX_SPEED"
            .toElementFilterExpression()
            .matches(node(tags = mapOf(*tags)))

    private fun matchesInSlowZone(vararg tags: Pair<String, String>): Boolean =
        "nodes with $FILTER_IS_IN_SLOW_ZONE"
            .toElementFilterExpression()
            .matches(node(tags = mapOf(*tags)))
}
