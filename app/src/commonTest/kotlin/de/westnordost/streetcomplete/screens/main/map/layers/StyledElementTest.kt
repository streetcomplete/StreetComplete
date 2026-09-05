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
        ).toGeoJsonFeatures()

        val feature = features.single()
        assertIs<Point>(feature.geometry)
        assertEquals(ElementKey(ElementType.NODE, 5), feature.properties.toElementKey())
        assertTrue(feature.properties.isDisabled())
        assertEquals(
            plainStyleImageId(Res.drawable.preset_maki_circle),
            (feature.properties["icon"] as JsonPrimitive).content,
        )
        assertEquals("Cafe", (feature.properties["label"] as JsonPrimitive).content)
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
        ).toGeoJsonFeatures()

        assertEquals(2, features.size)
        val area = features.first().properties
        assertEquals("rgba(255, 0, 0, 1.0)", (area["color"] as JsonPrimitive).content)
        assertEquals("rgba(171, 0, 0, 1.0)", (area["outline-color"] as JsonPrimitive).content)
        assertEquals(0.8f, (area["opacity"] as JsonPrimitive).content.toFloat())
        assertEquals(12f, (area["height"] as JsonPrimitive).content.toFloat())
        assertEquals(12f, (area["min-height"] as JsonPrimitive).content.toFloat())
        assertIs<Point>(features.last().geometry)
        assertEquals(ElementKey(ElementType.WAY, 9), features.last().properties.toElementKey())
    }

    @Test fun invisiblePolygonSuppressesExtrusion() {
        val ring = listOf(
            LatLon(0.0, 0.0), LatLon(0.0, 1.0), LatLon(1.0, 1.0), LatLon(0.0, 0.0)
        )
        val properties = StyledElement(
            element = Way(9, listOf(1, 2, 3, 1)),
            geometry = ElementPolygonsGeometry(listOf(ring), LatLon(0.5, 0.5)),
            style = OverlayStyle.Polygon(OverlayColor.Invisible, height = 12f),
        ).toGeoJsonFeatures().single().properties

        assertEquals(0f, (properties["opacity"] as JsonPrimitive).content.toFloat())
        assertNull(properties["color"])
        assertNull(properties["height"])
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
        ).toGeoJsonFeatures()

        assertEquals(4, features.size)
        val left = features[0].properties
        val right = features[1].properties
        val center = features[2].properties
        val label = features[3].properties
        assertEquals(-4.5f, (left["offset"] as JsonPrimitive).content.toFloat())
        assertTrue((left["dashed"] as JsonPrimitive).content.toBoolean())
        assertEquals(4.5f, (right["offset"] as JsonPrimitive).content.toFloat())
        assertEquals(0f, (right["opacity"] as JsonPrimitive).content.toFloat())
        assertEquals(6f, (center["width"] as JsonPrimitive).content.toFloat())
        assertTrue((center["bridge"] as JsonPrimitive).content.toBoolean())
        assertEquals(ElementKey(ElementType.WAY, 7), center.toElementKey())
        assertEquals("Primary", (label["label"] as JsonPrimitive).content)
        assertNull(label.toElementKey())
    }

    @Test fun roadWidthsAndBridgeDetectionMatchLegacyRules() {
        assertEquals(8f, getLineWidth(mapOf("highway" to "motorway")))
        assertEquals(1f, getLineWidth(mapOf("highway" to "cycleway")))
        assertEquals(2f, getLineWidth(emptyMap()))
        assertEquals(4f, getLineWidth(mapOf("highway" to "residential")))
        assertTrue(isBridge(mapOf("bridge" to "yes")))
        assertFalse(isBridge(mapOf("bridge" to "no")))
    }
}
