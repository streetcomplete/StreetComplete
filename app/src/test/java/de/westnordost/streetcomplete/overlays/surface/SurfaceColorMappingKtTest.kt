package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.createSurfaceAndNote
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.*
import org.junit.Test

class SurfaceColorMappingKtTest {

    @Test fun `return color for normal surfaces`() {
        val road = way(tags = mapOf("surface" to "asphalt"))
        assertEquals(Surface.ASPHALT.color, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return missing-data color for unpaved without note`() {
        val road = way(tags = mapOf("surface" to "unpaved"))
        assertEquals(Color.DATA_REQUESTED, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return missing-data color for paved without note`() {
        val road = way(tags = mapOf("surface" to "paved"))
        assertEquals(Color.DATA_REQUESTED, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return missing-data color for missing surface`() {
        val road = way(tags = mapOf())
        assertEquals(Color.DATA_REQUESTED, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return black for unpaved with note`() {
        val road = way(tags = mapOf(
            "surface" to "unpaved",
            "surface:note" to "note text",
        ))
        assertEquals(Color.BLACK, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return black for paved with note`() {
        val road = way(tags = mapOf(
            "surface" to "paved",
            "surface:note" to "note text",
        ))
        assertEquals(Color.BLACK, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return invisible for unpaved with restricted access`() {
        val road = way(tags = mapOf(
            "access" to "private",
            "surface" to "unpaved",
        ))
        assertEquals(Color.INVISIBLE, createSurfaceAndNote(road.tags).getColor(road))
    }

    @Test fun `return invisible for paved with restricted access`() {
        val road = way(tags = mapOf(
            "access" to "private",
            "surface" to "paved",
        ))
        assertEquals(Color.INVISIBLE, createSurfaceAndNote(road.tags).getColor(road))
    }
}
