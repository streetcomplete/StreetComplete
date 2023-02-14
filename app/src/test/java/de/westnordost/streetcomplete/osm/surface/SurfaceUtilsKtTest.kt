package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.overlays.Color.BLACK
import de.westnordost.streetcomplete.overlays.Color.INVISIBLE
import de.westnordost.streetcomplete.overlays.surface.color
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Test

class SurfaceUtilsKtTest {
    @Test
    fun `correct colour is being returned for basic surfaces`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "sidewalk" to "both",
            "surface" to "asphalt",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), Surface.ASPHALT.color)
    }

    @Test
    fun `correct colour is being returned for unpaved in basic situation`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "sidewalk" to "both",
            "surface" to "unpaved",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), Surface.UNPAVED_ROAD.color)
    }

    @Test
    fun `missing surface is treated like underspecified surface`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "sidewalk" to "both",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), Surface.UNPAVED_ROAD.color)
    }

    @Test
    fun `surface=cobblestone underspecified surface`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "sidewalk" to "both",
            "surface" to "cobblestone",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), Surface.UNPAVED_ROAD.color)
    }

    @Test
    fun `black is being returned for unpaved with note`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "sidewalk" to "both",
            "surface" to "unpaved",
            "surface:note" to "note text",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), BLACK)
    }

    @Test
    fun `invisible is being returned for unpaved with restricted access`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "access" to "private",
            "surface" to "unpaved",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), INVISIBLE)
    }

    @Test
    fun `invisible is not being returned for unpaved with restricted access but with foot access`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "access" to "private",
            "foot" to "yes",
            "surface" to "unpaved",
        ))
        assertEquals(createMainSurfaceStatus(road.tags).getItsColor(road), Surface.UNPAVED_ROAD.color)
    }
}
