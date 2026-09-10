package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ElementGeometryConversionTest {

    @Test fun convertsPoint() {
        val geometry = ElementPointGeometry(LatLon(1.0, 2.0)).toGeometry()

        assertEquals(Point(Position(2.0, 1.0)), geometry)
    }

    @Test fun convertsSingleAndMultiplePolylines() {
        val first = listOf(LatLon(1.0, 2.0), LatLon(3.0, 4.0))
        val second = listOf(LatLon(5.0, 6.0), LatLon(7.0, 8.0))

        assertIs<LineString>(ElementPolylinesGeometry(listOf(first), first[0]).toGeometry())
        assertIs<MultiLineString>(
            ElementPolylinesGeometry(listOf(first, second), first[0]).toGeometry()
        )
    }

    @Test fun convertsSinglePolygonWithItsHoles() {
        val outer = square(0.0, 0.0, 10.0)
        val hole = square(2.0, 2.0, 2.0).reversed()

        val geometry = ElementPolygonsGeometry(listOf(outer, hole), outer[0]).toGeometry()

        val polygon = assertIs<Polygon>(geometry)
        assertEquals(listOf(outer, hole).map(::positions), polygon.coordinates)
    }

    @Test fun assignsEachHoleToItsContainingOuterPolygon() {
        val smallOuter = square(0.0, 0.0, 10.0)
        val smallHole = square(2.0, 2.0, 2.0).reversed()
        val largeOuter = square(20.0, 20.0, 20.0)
        val largeHole = square(25.0, 25.0, 2.0).reversed()

        val geometry = ElementPolygonsGeometry(
            listOf(largeOuter, smallHole, smallOuter, largeHole),
            smallOuter[0],
        ).toGeometry()

        val multiPolygon = assertIs<MultiPolygon>(geometry)
        assertEquals(
            listOf(
                listOf(positions(smallOuter), positions(smallHole)),
                listOf(positions(largeOuter), positions(largeHole)),
            ),
            multiPolygon.coordinates
        )
    }

    private fun square(south: Double, west: Double, size: Double) = listOf(
        LatLon(south, west),
        LatLon(south + size, west),
        LatLon(south + size, west + size),
        LatLon(south, west + size),
        LatLon(south, west),
    )

    private fun positions(ring: List<LatLon>) = ring.map { Position(it.longitude, it.latitude) }
}
