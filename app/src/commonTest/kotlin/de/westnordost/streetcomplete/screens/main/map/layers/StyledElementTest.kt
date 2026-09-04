package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.ui.graphics.Color
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StyledElementTest {

    @Test fun pointCarriesIconLabelElementKeyAndDisabledState() {
        val position = LatLon(1.0, 2.0)
        val features = StyledElement(
            element = Node(5, position),
            geometry = ElementPointGeometry(position),
            style = OverlayStyle.Point(
                icon = Res.drawable.preset_maki_circle,
                label = "Cafe",
                disabled = true,
            ),
        ).toOverlayFeatures()

        val feature = features.single()
        assertIs<Point>(feature.geometry)
        assertEquals(ElementKey(ElementType.NODE, 5), feature.properties.toOverlayElementKey())
        assertTrue(feature.properties.isOverlayElementDisabled())
        assertEquals(
            plainStyleImageId(Res.drawable.preset_maki_circle),
            (feature.properties[ICON] as JsonPrimitive).content,
        )
        assertEquals("Cafe", (feature.properties[LABEL] as JsonPrimitive).content)
    }

    @Test fun visiblePolygonCarriesColorOutlineHeightAndClampedBase() {
        val ring = listOf(
            LatLon(0.0, 0.0), LatLon(0.0, 1.0), LatLon(1.0, 1.0), LatLon(0.0, 0.0)
        )
        val features = StyledElement(
            element = Way(9, listOf(1, 2, 3, 1)),
            geometry = ElementPolygonsGeometry(listOf(ring), LatLon(0.5, 0.5)),
            style = OverlayStyle.Polygon(
                color = Color.Red,
                icon = Res.drawable.preset_maki_circle,
                label = "Building",
                height = 12f,
                minHeight = 20f,
            ),
        ).toOverlayFeatures()

        assertEquals(2, features.size)
        val area = features.first().properties
        assertEquals("rgba(255, 0, 0, 1.0)", (area[COLOR] as JsonPrimitive).content)
        assertEquals("rgba(171, 0, 0, 1.0)", (area[OUTLINE_COLOR] as JsonPrimitive).content)
        assertEquals(0.8f, (area[OPACITY] as JsonPrimitive).content.toFloat())
        assertEquals(12f, (area[HEIGHT] as JsonPrimitive).content.toFloat())
        assertEquals(12f, (area[MIN_HEIGHT] as JsonPrimitive).content.toFloat())
        assertIs<Point>(features.last().geometry)
        assertEquals(ElementKey(ElementType.WAY, 9), features.last().properties.toOverlayElementKey())
    }

    @Test fun invisiblePolygonSuppressesExtrusion() {
        val ring = listOf(
            LatLon(0.0, 0.0), LatLon(0.0, 1.0), LatLon(1.0, 1.0), LatLon(0.0, 0.0)
        )
        val properties = StyledElement(
            element = Way(9, listOf(1, 2, 3, 1)),
            geometry = ElementPolygonsGeometry(listOf(ring), LatLon(0.5, 0.5)),
            style = OverlayStyle.Polygon(OverlayColor.Invisible, height = 12f),
        ).toOverlayFeatures().single().properties

        assertEquals(0f, (properties[OPACITY] as JsonPrimitive).content.toFloat())
        assertNull(properties[COLOR])
        assertNull(properties[HEIGHT])
    }

    @Test fun polylineCreatesOrderedSideCenterAndLabelFeatures() {
        val line = listOf(LatLon(0.0, 0.0), LatLon(1.0, 1.0))
        val features = StyledElement(
            element = Way(
                id = 7,
                nodeIds = listOf(1, 2),
                tags = mapOf("highway" to "primary", "bridge" to "yes"),
            ),
            geometry = ElementPolylinesGeometry(listOf(line), LatLon(0.5, 0.5)),
            style = OverlayStyle.Polyline(
                stroke = OverlayStyle.Stroke(OverlayColor.Blue),
                strokeLeft = OverlayStyle.Stroke(OverlayColor.Gold, dashed = true),
                strokeRight = OverlayStyle.Stroke(OverlayColor.Invisible),
                label = "Primary",
            ),
        ).toOverlayFeatures()

        assertEquals(4, features.size)
        val left = features[0].properties
        val right = features[1].properties
        val center = features[2].properties
        val label = features[3].properties
        assertEquals(-4.5f, (left[OFFSET] as JsonPrimitive).content.toFloat())
        assertTrue((left[DASHED] as JsonPrimitive).content.toBoolean())
        assertEquals(4.5f, (right[OFFSET] as JsonPrimitive).content.toFloat())
        assertEquals(0f, (right[OPACITY] as JsonPrimitive).content.toFloat())
        assertEquals(6f, (center[WIDTH] as JsonPrimitive).content.toFloat())
        assertTrue((center[BRIDGE] as JsonPrimitive).content.toBoolean())
        assertEquals(ElementKey(ElementType.WAY, 7), center.toOverlayElementKey())
        assertEquals("Primary", (label[LABEL] as JsonPrimitive).content)
        assertNull(label.toOverlayElementKey())
    }

    @Test fun roadWidthsAndBridgeDetectionMatchLegacyRules() {
        assertEquals(8f, overlayLineWidth(mapOf("highway" to "motorway")))
        assertEquals(1f, overlayLineWidth(mapOf("highway" to "cycleway")))
        assertEquals(2f, overlayLineWidth(emptyMap()))
        assertEquals(4f, overlayLineWidth(mapOf("highway" to "residential")))
        assertTrue(isOverlayBridge(mapOf("bridge" to "yes")))
        assertFalse(isOverlayBridge(mapOf("bridge" to "no")))
    }
}
