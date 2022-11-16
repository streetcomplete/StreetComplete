package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert
import org.junit.Test

class PathSurfaceOverlayKtTest {
    @Test
    fun `matching way is found`() {
        val data = way(tags = mapOf(
            "highway" to "path",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        Assert.assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `eligible for roads with tagged sidewalks`() {
        val data = way(tags = mapOf(
            "highway" to "motorway",
            "sidewalk" to "both",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        Assert.assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
    }

    @Test
    fun `ineligible for roads without tagged sidewalks`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        Assert.assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `way with surface note without surface tag is ineligible`() {
        val data = way(tags = mapOf(
            "highway" to "path",
            "surface:note" to "explanation for missing surface tag",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        Assert.assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }

    @Test
    fun `way with surface note and surface tag is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "path",
            "surface" to "paved",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        Assert.assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 1)
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
        Assert.assertTrue(styleWithNote is PolylineStyle)
        Assert.assertTrue(styleWithoutNote is PolylineStyle)
        if (styleWithNote is PolylineStyle && styleWithoutNote is PolylineStyle) {
            Assert.assertNotEquals(styleWithNote.stroke!!.color, styleWithoutNote.stroke!!.color)
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
        Assert.assertEquals(PathSurfaceOverlay().getStyledElements(mapData).toList().size, 0)
    }
}
