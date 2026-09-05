package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.resources.scissors_cut
import de.westnordost.streetcomplete.ui.common.quest.Marker
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GeometryMarkersLayersTest {

    @Test fun pointMarkerGetsNativeSymbolFeature() {
        val position = LatLon(1.0, 2.0)

        val feature = Marker(ElementPointGeometry(position)).toGeometryMarkerFeatures().single()

        assertIs<Point>(feature.geometry)
        assertEquals(
            JsonPrimitive(plainStyleImageId(Res.drawable.preset_maki_circle)),
            assertNotNull(feature.properties["icon-image"]),
        )
    }

    @Test fun lineWithIconAndTitleGetsGeometry() {
        val line = listOf(LatLon(1.0, 2.0), LatLon(3.0, 4.0))
        val center = LatLon(5.0, 6.0)

        val features = Marker(
            geometry = ElementPolylinesGeometry(listOf(line), center),
            icon = Res.drawable.scissors_cut,
            title = "Cut here",
        ).toGeometryMarkerFeatures()

        assertEquals(2, features.size)
        assertIs<Point>(features[0].geometry)
        assertEquals(JsonPrimitive("Cut here"), features[0].properties["label"])
        assertIs<LineString>(features[1].geometry)
    }

    @Test fun lineWithoutIconOrTitleOnlyGetsGeometry() {
        val line = listOf(LatLon(1.0, 2.0), LatLon(3.0, 4.0))

        val features = Marker(
            ElementPolylinesGeometry(listOf(line), line.first())
        ).toGeometryMarkerFeatures()

        assertEquals(1, features.size)
        assertIs<LineString>(features.single().geometry)
    }
}
