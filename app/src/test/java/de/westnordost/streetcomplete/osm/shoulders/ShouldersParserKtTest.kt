package de.westnordost.streetcomplete.osm.shoulders

import kotlin.test.Test
import kotlin.test.assertEquals

class ShouldersParserKtTest {
    @Test fun `shoulders with normal tagging`() {
        assertEquals(null, createShoulders(mapOf(), false))

        assertEquals(
            Shoulders(left = true, right = false),
            createShoulders(mapOf("shoulder" to "left"), false)
        )
        assertEquals(
            Shoulders(left = false, right = true),
            createShoulders(mapOf("shoulder" to "right"), false)
        )
        assertEquals(
            Shoulders(left = true, right = true),
            createShoulders(mapOf("shoulder" to "both"), false)
        )
        assertEquals(
            Shoulders(left = true, right = true),
            createShoulders(mapOf("shoulder" to "yes"), false)
        )
        assertEquals(
            Shoulders(left = false, right = false),
            createShoulders(mapOf("shoulder" to "no"), false)
        )
    }

    @Test fun `shoulders with yes-tagging in oneways`() {
        assertEquals(
            Shoulders(left = false, right = true),
            createShoulders(mapOf("shoulder" to "yes", "oneway" to "yes"), false)
        )
        assertEquals(
            Shoulders(left = true, right = false),
            createShoulders(mapOf("shoulder" to "yes", "oneway" to "-1"), false)
        )
        assertEquals(
            Shoulders(left = true, right = false),
            createShoulders(mapOf("shoulder" to "yes", "oneway" to "yes"), true)
        )
        assertEquals(
            Shoulders(left = false, right = true),
            createShoulders(mapOf("shoulder" to "yes", "oneway" to "-1"), true)
        )
    }

    @Test fun `shoulders with alternative tagging`() {
        assertEquals(
            Shoulders(left = true, right = true),
            createShoulders(mapOf("shoulder:both" to "yes"), false)
        )

        assertEquals(
            Shoulders(left = false, right = true),
            createShoulders(mapOf("shoulder:right" to "yes"), false)
        )

        assertEquals(
            Shoulders(left = true, right = false),
            createShoulders(mapOf("shoulder:left" to "yes"), false)
        )

        assertEquals(
            Shoulders(left = true, right = true),
            createShoulders(mapOf("shoulder:left" to "yes", "shoulder:right" to "yes"), false)
        )

        assertEquals(
            Shoulders(left = false, right = false),
            createShoulders(mapOf("shoulder:both" to "no"), false)
        )

        assertEquals(
            Shoulders(left = false, right = false),
            createShoulders(mapOf("shoulder:right" to "no"), false)
        )

        assertEquals(
            Shoulders(left = false, right = false),
            createShoulders(mapOf("shoulder:left" to "no"), false)
        )
    }

    @Test fun `shoulders with motorway`() {
        assertEquals(
            Shoulders(left = true, right = true),
            createShoulders(mapOf("highway" to "motorway"), false)
        )
        assertEquals(
            Shoulders(left = false, right = true),
            createShoulders(mapOf("highway" to "motorway", "oneway" to "yes"), false)
        )
    }
}
