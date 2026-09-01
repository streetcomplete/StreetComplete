package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.resources.scissors_cut
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GeometryMarkersLayersTest {

    @Test fun pointMarkerGetsDefaultIcon() {
        val position = LatLon(1.0, 2.0)

        val feature = Marker(ElementPointGeometry(position)).toGeometryMarkerFeatures().single()

        assertEquals(Point(Position(2.0, 1.0)), feature.geometry)
        assertEquals(JsonPrimitive("preset_maki_circle"), feature.properties["icon"])
    }

    @Test fun lineWithIconAndTitleGetsCenterSymbolAndGeometry() {
        val line = listOf(LatLon(1.0, 2.0), LatLon(3.0, 4.0))
        val center = LatLon(5.0, 6.0)

        val features = Marker(
            geometry = ElementPolylinesGeometry(listOf(line), center),
            icon = Res.drawable.scissors_cut,
            title = "Cut here",
        ).toGeometryMarkerFeatures()

        assertEquals(2, features.size)
        assertEquals(Point(Position(6.0, 5.0)), features[0].geometry)
        assertEquals(JsonPrimitive("scissors_cut"), features[0].properties["icon"])
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

    @Test fun drawableResourcesResolveToStyleIds() {
        assertEquals("preset_maki_circle", Res.drawable.preset_maki_circle.id)
        assertEquals("scissors_cut", Res.drawable.scissors_cut.id)
    }
}
