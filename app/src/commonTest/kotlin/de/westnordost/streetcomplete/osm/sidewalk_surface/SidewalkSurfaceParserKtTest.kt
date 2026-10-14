package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.surface.Surface.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SidewalkSurfaceParserKtTest {

    @Test fun `generic surface applies only to existing sidewalk side`() {
        assertEquals(
            SidewalkSurface(Sides(ASPHALT, null)),
            parseSidewalksSurface(mapOf(
                "sidewalk" to "left",
                "sidewalk:surface" to "asphalt",
            ))
        )
        assertEquals(
            SidewalkSurface(Sides(null, ASPHALT)),
            parseSidewalksSurface(mapOf(
                "sidewalk" to "right",
                "sidewalk:surface" to "asphalt",
            ))
        )
    }

    @Test fun `surface is not applied to separately mapped sidewalk`() {
        assertEquals(
            SidewalkSurface(Sides(null, ASPHALT)),
            parseSidewalksSurface(mapOf(
                "sidewalk:left" to "separate",
                "sidewalk:right" to "yes",
                "sidewalk:surface" to "asphalt",
            ))
        )
    }
}
