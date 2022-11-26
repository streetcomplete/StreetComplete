package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PathSurfaceOverlayKtTest {
    @Test
    fun `matching way is found`() {
        val data = way(tags = mapOf(
            "highway" to "path",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `eligible for roads with tagged sidewalks`() {
        val data = way(tags = mapOf(
            "highway" to "motorway",
            "sidewalk" to "both",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `ineligible for roads without tagged sidewalks`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `ineligible for paths with tagged sidewalks`() {
        // popular tagging style in Netherlands - see http://overpass-turbo.eu/s/1oel
        val data = way(tags = mapOf(
            "highway" to "cycleway",
            "sidewalk" to "both",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `way with surface note without surface tag is ineligible`() {
        val data = way(tags = mapOf(
            "highway" to "path",
            "surface:note" to "explanation for missing surface tag",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `way with surface note and surface tag is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "path",
            "surface" to "paved",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `adding surface note to unspecified surface changes line colour`() {
        val withNoteData = way(tags = mapOf(
            "highway" to "path",
            "surface" to "paved",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val withoutNoteData = way(tags = mapOf(
            "highway" to "path",
            "surface" to "paved",
        ))
        val mapDataWithNote = TestMapDataWithGeometry(listOf(withNoteData))
        val mapDataWithoutNote = TestMapDataWithGeometry(listOf(withoutNoteData))
        val styleWithNote = PathSurfaceOverlay().getStyledElements(mapDataWithNote).first().second
        val styleWithoutNote = PathSurfaceOverlay().getStyledElements(mapDataWithoutNote).first().second
        assertTrue(styleWithNote is PolylineStyle)
        assertTrue(styleWithoutNote is PolylineStyle)
        if (styleWithNote is PolylineStyle && styleWithoutNote is PolylineStyle) {
            assertNotEquals(styleWithNote.stroke!!.color, styleWithoutNote.stroke!!.color)
        }
    }

    @Test
    fun `way with unsupported surface is ineligible`() {
        val data = way(tags = mapOf(
            "highway" to "path",
            "surface" to "https://en.wikipedia.org/wiki/Stone_frigate",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `test acceptance of more complex data`() {
        // https://www.openstreetmap.org/way/395502477 version 8
        val data = way(tags = mapOf(
            "cycleway:surface" to "concrete",
            "footway:surface" to "paved",
            "footway:surface:note" to "incrustations de gravier sur béton",
            "highway" to "path",
            "segregated" to "yes",
            "surface" to "concrete",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `test acceptance of surface notes without surface on segregated ways`() {
        // https://www.openstreetmap.org/way/395502477 version 8
        val data = way(tags = mapOf(
            "cycleway:surface" to "concrete",
            "footway:surface" to "dirt",
            "surface:note" to "really? really?",
            "highway" to "path",
            "segregated" to "yes",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `test acceptance of surface notes without surface on real complicated way that seemed buggy`() {
        // https://www.openstreetmap.org/way/925626513 version 4
        val data = way(tags = mapOf(
            "bicycle" to "designated",
            "cycleway:surface" to "paving_stones",
            "foot" to "designated",
            "footway:surface" to "asphalt",
            "highway" to "path",
            "lit" to "yes",
            "oneway:bicycle" to "yes",
            "path" to "sidewalk",
            "segregated" to "yes",
            "surface" to "paving_stones",
            "surface:note" to "Rad Pflastersteine Fußgänger Asphalt",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `test acceptance of surface notes without surface on real complicated way that seemed buggy - with footway set to dirt`() {
        // https://www.openstreetmap.org/way/925626513 version 4
        val data = way(tags = mapOf(
            "bicycle" to "designated",
            "cycleway:surface" to "paving_stones",
            "check_date:cycleway:surface" to "2022-11-19",
            "foot" to "designated",
            "footway:surface" to "dirt",
            "highway" to "path",
            "lit" to "yes",
            "oneway:bicycle" to "yes",
            "path" to "sidewalk",
            "segregated" to "yes",
            "surface:note" to "Rad Pflastersteine Fußgänger Asphalt",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `way with surface tags being replaced is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "footway",
            "surface" to "cobblestone",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `way with sidewalk surface tags on one side is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "sidewalk" to "both",
            "sidewalk:right:surface" to "paved",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `way with right-handed surface note sidewalk tags is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "sidewalk" to "both",
            "sidewalk:right:surface" to "paved",
            "sidewalk:right:surface:note" to "aaarghhhhhhh",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `way with both-sided surface note sidewalk tags is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "sidewalk" to "both",
            "sidewalk:both:surface" to "paved",
            "sidewalk:both:surface:note" to "aaarghhhhhhh",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `way with sidewalk surface tags and without sidewalk tags is ineligible`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "sidewalk:both:surface" to "paved",
            "sidewalk:both:surface:note" to "aaarghhhhhhh",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `path with unhandled surface is rejected`() {
        val data = way(tags = mapOf(
            "highway" to "path",
            "surface" to "surprise Japanese torpedo boats",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `road with unhandled sidewalk surface is rejected`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "sidewalk" to "both",
            "sidewalk:both:surface" to "surprise Japanese torpedo boats",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }
}
