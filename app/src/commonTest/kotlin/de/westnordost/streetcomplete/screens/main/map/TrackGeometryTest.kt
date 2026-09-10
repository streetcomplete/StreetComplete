package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TrackGeometryTest {

    @Test fun lineRequiresAtLeastTwoPoints() {
        assertNull(emptyList<LatLon>().toLineGeometry())
        assertNull(listOf(LatLon(1.0, 2.0)).toLineGeometry())
    }

    @Test fun lineUsesLongitudeLatitudeOrder() {
        val line = listOf(LatLon(1.0, 2.0), LatLon(3.0, 4.0)).toLineGeometry()

        assertEquals(listOf(Position(2.0, 1.0), Position(4.0, 3.0)), line?.coordinates)
    }

    @Test fun multiLineDropsIncompleteSegments() {
        val lines = listOf(
            listOf(LatLon(1.0, 2.0)),
            listOf(LatLon(3.0, 4.0), LatLon(5.0, 6.0)),
        ).toMultiLineGeometry()

        assertEquals(listOf(listOf(Position(4.0, 3.0), Position(6.0, 5.0))), lines.coordinates)
    }

    @Test fun interpolationUsesShortestAntimeridianPath() {
        assertEquals(
            LatLon(15.0, -180.0),
            interpolateLatLon(LatLon(10.0, 179.0), LatLon(20.0, -179.0), 0.5)
        )
    }
}
