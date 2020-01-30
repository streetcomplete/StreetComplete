package de.westnordost.streetcomplete.util

import org.junit.Test

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon

import org.junit.Assert.*
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SphericalEarthMathTest {

    /* ++++++++++++++++++++++++++++++++ test distance functions +++++++++++++++++++++++++++++++++ */

    @Test fun `distance to Berlin`() {
        checkHamburgTo(52.4, 13.4, 259, 117, 120)
    }

    @Test fun `distance to Lübeck`() {
        checkHamburgTo(53.85, 10.68, 59, 49, 49)
    }

    @Test fun `distance to Los Angeles`() {
        checkHamburgTo(34.0, -118.0, 9075, 319, 208)
    }

    @Test fun `distance to Reykjavik`() {
        checkHamburgTo(64.11, -21.98, 2152, 316, 288)
    }

    @Test fun `distance to Port Elizabeth`() {
        checkHamburgTo(-33.9, -25.6, 10307, 209, 200)
    }

    @Test fun `distance to Poles`() {
        checkHamburgTo(90.0, 123.0, 4059, 0, null)
        checkHamburgTo(-90.0, 0.0, 15956, 180, null)
    }

    @Test fun `distance to other side of Earth`() {
        checkHamburgTo(-53.5, -170.0, (PI * 6371).toInt(), 270, 270)
    }

    @Test fun `short distance`() {
        val one = p(9.9782365, 53.5712482)
        val two = p(9.9782517, 53.5712528)
        assertEquals(1, one.distanceTo(two).toInt())
    }

    @Test fun `distance of polyline is zero for one position`() {
        assertEquals(0.0, listOf(p(0.0, 0.0)).measuredLength(), 0.0)
    }

    @Test fun `distance of polyline for two positions`() {
        val p0 = p(0.0, 0.0)
        val p1 = p(1.0, 1.0)
        assertEquals(p0.distanceTo(p1), listOf(p0, p1).measuredLength(), 0.0)
    }

    @Test fun `distance of polyline for three positions`() {
        val p0 = p(0.0, 0.0)
        val p1 = p(1.0, 1.0)
        val p2 = p(2.0, 2.0)
        val positions = listOf(p0, p1, p2)
        assertEquals(
            p0.distanceTo(p1) + p1.distanceTo(p2),
            positions.measuredLength(),
            1e-16
        )
    }

    private fun checkHamburgTo(lat: Double, lon: Double, dist: Int, angle: Int, angle2: Int?) {
        val t = p(lon, lat)

        assertEquals(dist, (HH.distanceTo(t) / 1000).roundToInt())
        assertEquals(dist, (t.distanceTo(HH) / 1000).roundToInt())

        assertEquals(angle, HH.initialBearingTo(t).roundToInt())
        if (angle2 != null) assertEquals(angle2, HH.finalBearingTo(t).roundToInt())
    }

    /* ++++++++++++++++++++++++++++++ test distance to arc distance +++++++++++++++++++++++++++++ */

    @Test fun `simple distance to horizontal arc`() {
        val start = OsmLatLon(0.0, -0.01)
        val end = OsmLatLon(0.0, +0.01)
        val point = OsmLatLon(0.01, 0.0)
        val intersect = OsmLatLon(0.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `simple distance to vertical arc`() {
        val start = OsmLatLon(-0.01, 0.0)
        val end = OsmLatLon(+0.01, 0.0)
        val point = OsmLatLon(0.0, 0.01)
        val intersect = OsmLatLon(0.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `simple distance to sloped arc`() {
        val start = OsmLatLon(-0.01, -0.01)
        val end = OsmLatLon(+0.01, +0.01)
        val point = OsmLatLon(-0.01, +0.01)
        val intersect = OsmLatLon(0.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance to horizontal arc crossing 180th meridian`() {
        val start = OsmLatLon(0.0, 170.0)
        val end = OsmLatLon(0.0, -170.0)
        val point = OsmLatLon(0.01, -175.0)
        val intersect = OsmLatLon(0.0, -175.0)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance to vertical arc crossing north pole`() {
        val start = OsmLatLon(80.0, 0.0)
        val end = OsmLatLon(0.0, 180.0)
        val point = OsmLatLon(85.0, 179.99)
        val intersect = OsmLatLon(85.0, 180.0)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance to single position`() {
        val point = OsmLatLon(0.01, 0.0)
        val intersect = OsmLatLon(0.0, 0.0)
        assertEquals(
            point.distanceTo(intersect),
            point.crossTrackDistanceTo(listOf(intersect)),
            0.01
        )
    }

    @Test fun `distance to single arc`() {
        val start = OsmLatLon(0.0, -0.01)
        val end = OsmLatLon(0.0, +0.01)
        val point = OsmLatLon(0.01, 0.0)
        assertEquals(
            point.crossTrackDistanceTo(start, end),
            point.crossTrackDistanceTo(listOf(start, end)),
            0.01
        )
    }

    @Test fun `distance to multiple arcs`() {
        val p0 = OsmLatLon(0.0, -0.01)
        val p1 = OsmLatLon(0.0, +0.01)
        val p2 = OsmLatLon(0.0, +0.02)
        val point = OsmLatLon(0.01, 0.0)
        assertEquals(
            point.crossTrackDistanceTo(p0, p1),
            point.crossTrackDistanceTo(listOf(p0, p1, p2)),
            0.01
        )
        assertEquals(
            point.crossTrackDistanceTo(p0, p1),
            point.crossTrackDistanceTo(listOf(p2, p1, p0)),
            0.01
        )
    }

    /* +++++++++++++++++++++++++++++ test creation of bounding boxes ++++++++++++++++++++++++++++ */

    @Test fun `enclosingBoundingBox radius`() {
        val pos = p(0.0, 0.0)
        val bbox = pos.enclosingBoundingBox(5000.0)

        val dist = (sqrt(2.0) * 5000).toInt()

        // all four corners of the bbox should be 'radius' away
        assertEquals(dist, pos.distanceTo(bbox.min).roundToInt())
        assertEquals(dist, pos.distanceTo(bbox.max).roundToInt())
        assertEquals(
            dist,
            pos.distanceTo(p(bbox.maxLongitude, bbox.minLatitude)).roundToInt()
        )
        assertEquals(
            dist,
            pos.distanceTo(p(bbox.minLongitude, bbox.maxLatitude)).roundToInt()
        )

        assertEquals(225, pos.initialBearingTo(bbox.min).roundToInt())
        assertEquals(45, pos.initialBearingTo(bbox.max).roundToInt())
    }

    @Test fun `enclosingBoundingBox crosses 180th meridian`() {
        val bbox = p(180.0, 0.0).enclosingBoundingBox(5000.0)

        assertTrue(bbox.crosses180thMeridian())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `enclosingBoundingBox fails for empty line`() {
        listOf<LatLon>().enclosingBoundingBox()
    }

    @Test fun `enclosingBoundingBox for line`() {
        val positions = listOf(p(0.0, -4.0), p(3.0, 12.0), p(16.0, 1.0),p(-6.0, 0.0))
        val bbox = positions.enclosingBoundingBox()
        assertEquals(-4.0, bbox.minLatitude, 0.0)
        assertEquals(12.0, bbox.maxLatitude, 0.0)
        assertEquals(16.0, bbox.maxLongitude, 0.0)
        assertEquals(-6.0, bbox.minLongitude, 0.0)
    }

    @Test fun `enclosingBoundingBox for line crosses 180th meridian`() {
        val positions = listOf(p(160.0, 10.0),p(-150.0, 0.0),p(180.0, -10.0))
        val bbox = positions.enclosingBoundingBox()
        assertTrue(bbox.crosses180thMeridian())
        assertEquals(-10.0, bbox.minLatitude, 0.0)
        assertEquals(10.0, bbox.maxLatitude, 0.0)
        assertEquals(-150.0, bbox.maxLongitude, 0.0)
        assertEquals(160.0, bbox.minLongitude, 0.0)
    }

    /* ++++++++++++++++++++++++++++++ test translating of positions +++++++++++++++++++++++++++++ */

    @Test fun `translate latitude north`() { checkTranslate(1000, 0) }
    @Test fun `translate latitude south`() { checkTranslate(1000, 180) }
    @Test fun `translate latitude west`() { checkTranslate(1000, 270) }
    @Test fun `translate latitude east`() { checkTranslate(1000, 90) }
    @Test fun `translate latitude northeast`() { checkTranslate(1000, 45) }
    @Test fun `translate latitude southeast`() { checkTranslate(1000, 135) }
    @Test fun `translate latitude southwest`() { checkTranslate(1000, 225) }
    @Test fun `translate latitude northwest`() { checkTranslate(1000, 315) }

    @Test fun translateOverBoundaries() {
        // cross 180th meridian both ways
        checkTranslate(p(179.9999999, 0.0), 1000, 90)
        checkTranslate(p(-179.9999999, 0.0), 1000, 270)
        // cross north pole and come out on the other side
        // should come out at 45,-90
        val quarterOfEarth = (PI / 2 * EARTH_RADIUS).toInt()
        checkTranslate(p(90.0, +45.0), quarterOfEarth, 0)
        // should come out at -45,-90
        checkTranslate(p(90.0, -45.0), quarterOfEarth, 180)
    }

    private fun checkTranslate(one: LatLon, distance: Int, angle: Int) {
        val two = one.translate(distance.toDouble(), angle.toDouble())

        assertEquals(distance, one.distanceTo(two).roundToInt())
        assertEquals(angle, one.initialBearingTo(two).roundToInt())
    }

    private fun checkTranslate(distance: Int, angle: Int) {
        val one = p(9.9782365, 53.5712482)
        checkTranslate(one, distance, angle)
    }

    /* +++++++++++++++++++++++++++++ test calculation of center line ++++++++++++++++++++++++++++ */

    @Test(expected = IllegalArgumentException::class)
    fun `centerLineOfPolyline for point fails`() {
        listOf(p(0.0,0.0)).centerLineOfPolyline()
    }

    @Test fun `centerLineOfPolyline for a line with zero length`() {
        val p0 = p(0.0, 0.0)
        val p1 = p(0.0, 0.0)
        val p2 = p(0.0, 0.0)
        assertEquals(Pair(p0, p1), listOf(p0, p1, p2).centerLineOfPolyline())
    }

    @Test fun `centerLineOfPolyline for a single line`() {
        val p0 = p(0.0, 0.0)
        val p1 = p(1.0, 1.0)
        assertEquals(Pair(p0, p1), listOf(p0, p1).centerLineOfPolyline())
    }

    @Test fun `centerLineOfPolyline for a polyline where the center is the middle line`() {
        val p0 = p(0.0, 0.0)
        val p1 = p(1.0, 1.0)
        val p2 = p(2.0, 2.0)
        val p3 = p(3.0, 3.0)
        assertEquals(Pair(p1, p2), listOf(p0, p1, p2, p3).centerLineOfPolyline())
    }

    @Test fun `centerLineOfPolyline for a polyline where the center is not the middle line`() {
        val p0 = p(0.0, 0.0)
        val p1 = p(10.0, 10.0)
        val p2 = p(11.0, 11.0)
        val p3 = p(12.0, 12.0)
        assertEquals(Pair(p0, p1), listOf(p0, p1, p2, p3).centerLineOfPolyline())
    }

    /* +++++++++++++++++++++++++ test calculation of center point of line +++++++++++++++++++++++ */

    @Test(expected = IllegalArgumentException::class)
    fun `centerPointOfPolyline fails for empty list`() {
        listOf<LatLon>().centerPointOfPolyline()
    }

    @Test fun `centerPointOfPolyline for line with zero length`() {
        val p0 = p(20.0, 20.0)
        assertEquals(p0, listOf(p0,p0).centerPointOfPolyline())
    }

    @Test fun `centerPointOfPolyline for simple line`() {
        val polyline = listOf(p(-20.0, 80.0), p(20.0, -60.0))
        assertEquals(p(0.0, 10.0), polyline.centerPointOfPolyline())
    }

    @Test fun `centerPointOfPolyline for line that crosses 180th meridian`() {
        assertEquals(p(-170.0, 0.0), listOf(p(170.0, 0.0), p(-150.0, 0.0)).centerPointOfPolyline())
        assertEquals(p(170.0, 0.0), listOf(p(150.0, 0.0), p(-170.0, 0.0)).centerPointOfPolyline())
    }

    /* +++++++++++++++++++++++ test calculation of center point of polygon ++++++++++++++++++++++ */

    @Test(expected = IllegalArgumentException::class)
    fun `centerPointOfPolygon for empty polygon fails`() {
        listOf<LatLon>().centerPointOfPolygon()
    }

    @Test fun `centerPointOfPolygon with no area simply returns first point`() {
        val positions = listOf(p(10.0, 10.0), p(10.0, 20.0), p(10.0, 30.0))
        assertEquals(p(10.0, 10.0), positions.centerPointOfPolygon())
    }

    @Test fun `centerPointOfPolygon at origin`() {
        val center = p(0.0, 0.0)
        assertEquals(center, center.createRhombus(1.0).centerPointOfPolygon())
    }

    @Test fun `centerPointOfPolygon at 180th meridian`() {
        val center = p(179.9, 0.0)
        assertEquals(center, center.createRhombus(1.0).centerPointOfPolygon())
    }

    /* +++++++++++++++++++++++++ test calculation of point in line string +++++++++++++++++++++++ */

    @Test fun `pointOnPolylineFromStart for single line`() {
        val list = listOf(p(0.0, 0.0), p(10.0, 0.0))
        assertEquals(p(2.5, 0.0), list.pointOnPolylineFromStart(list.measuredLength() * 0.25))
    }

    @Test fun `pointOnPolylineFromStart for polyline`() {
        val list = listOf(p(0.0, 0.0), p(5.0, 0.0), p(10.0, 0.0))
        assertEquals(p(2.5, 0.0), list.pointOnPolylineFromStart(list.measuredLength() * 0.25))
    }

    @Test fun `pointOnPolylineFromStart for line that crosses 180th meridian`() {
        val list = listOf(p(179.0, 0.0), p(-179.0, 0.0))
        assertEquals(p(-180.0, 0.0), list.pointOnPolylineFromStart(list.measuredLength() * 0.5))
    }

    /* +++++++++++++++++++++++++++++++ test point in polygon check ++++++++++++++++++++++++++++++ */

    @Test fun `point at polygon vertex is in polygon`() {
        val square = p(0.0, 0.0).createSquareWithPointsAtCenterOfEdges(10.0)
        for (pos in square) {
            assertTrue(pos.isInPolygon(square))
        }
    }

    @Test fun `point at polygon vertex at 180th meridian is in polygon`() {
        val square = p(180.0, 0.0).createSquareWithPointsAtCenterOfEdges(10.0)
        for (pos in square) {
            assertTrue(pos.isInPolygon(square))
        }
    }

    @Test fun `point at polygon edge is in polygon`() {
        val square = p(0.0, 0.0).createSquare(10.0)
        assertTrue(p(0.0, 10.0).isInPolygon(square))
        assertTrue(p(10.0, 0.0).isInPolygon(square))
        assertTrue(p(-10.0, 0.0).isInPolygon(square))
        assertTrue(p(0.0, -10.0).isInPolygon(square))
    }

    @Test fun `point at polygon edge at 180th meridian is in polygon`() {
        val square = p(180.0, 0.0).createSquare(10.0)
        assertTrue(p(180.0, 10.0).isInPolygon(square))
        assertTrue(p(-170.0, 0.0).isInPolygon(square))
        assertTrue(p(170.0, 0.0).isInPolygon(square))
        assertTrue(p(180.0, -10.0).isInPolygon(square))
    }

    @Test fun `point in polygon is in polygon`() {
        assertTrue(p(0.0, 0.0).isInPolygon(
            listOf(p(1.0, 1.0), p(1.0, -2.0), p(-2.0, 1.0))
        ))
    }

    @Test fun `point in polygon at 180th meridian is in polygon`() {
        assertTrue(p(180.0, 0.0).isInPolygon(
            listOf(p(-179.0, 1.0), p(-179.0, -2.0), p(178.0, 1.0))
        ))
    }

    // The counting number algorithm in particular needs to handle a special case where the ray
    // intersects the polygon in a polygon vertex

    @Test fun `point in polygon whose ray insersects a vertex is in polygon`() {
        assertTrue(p(0.0, 0.0).isInPolygon(p(0.0, 0.0).createRhombus(1.0)))
    }

    @Test fun `point in polygon whose ray intersects a vertex at 180th meridian is in polygon`() {
        assertTrue(p(180.0, 0.0).isInPolygon(p(180.0, 0.0).createRhombus(1.0)))
    }

    @Test fun `point outside polygon whose ray intersects a vertex is outside polygon`() {
        val rhombus = p(0.0, 0.0).createRhombus(1.0)
        // four checks here because the ray could be cast in any direction
        assertFalse(p(-2.0, 1.0).isInPolygon(rhombus))
        assertFalse(p(-2.0, 0.0).isInPolygon(rhombus))
        assertFalse(p(1.0, -2.0).isInPolygon(rhombus))
        assertFalse(p(0.0, -2.0).isInPolygon(rhombus))
    }

    @Test fun `point outside polygon whose ray intersects a vertex at 180th meridian is outside polygon`() {
        val rhombus = p(180.0, 0.0).createRhombus(1.0)
        // four checks here because the ray could be cast in any direction
        assertFalse(p(178.0, 1.0).isInPolygon(rhombus))
        assertFalse(p(178.0, 0.0).isInPolygon(rhombus))
        assertFalse(p(-179.0, -2.0).isInPolygon(rhombus))
        assertFalse(p(180.0, -2.0).isInPolygon(rhombus))
    }

    @Test fun `point in polygon whose ray intersects polygon edges is inside polygon`() {
        val bonbon = p(0.0, 0.0).createBonbon()
        assertTrue(p(0.0, 0.0).isInPolygon(bonbon))
    }

    @Test fun `point in polygon whose ray intersects polygon edges at 180th meridian is inside polygon`() {
        val bonbon = p(180.0, 0.0).createBonbon()
        assertTrue(p(180.0, 0.0).isInPolygon(bonbon))
    }

    @Test fun `point outside polygon whose ray intersects polygon edges is outside polygon`() {
        val bonbon = p(0.0, 0.0).createBonbon()
        // four checks here because the ray could be cast in any direction
        assertFalse(p(-3.0, 0.0).isInPolygon(bonbon))
        assertFalse(p(+3.0, 0.0).isInPolygon(bonbon))
        assertFalse(p(0.0, +3.0).isInPolygon(bonbon))
        assertFalse(p(0.0, -3.0).isInPolygon(bonbon))
    }

    @Test fun `point outside polygon whose ray intersects polygon edges at 180th meridian is outside polygon`() {
        val bonbon = p(180.0, 0.0).createBonbon()
        // four checks here because the ray could be cast in any direction
        assertFalse(p(177.0, 0.0).isInPolygon(bonbon))
        assertFalse(p(-177.0, 0.0).isInPolygon(bonbon))
        assertFalse(p(180.0, +3.0).isInPolygon(bonbon))
        assertFalse(p(180.0, -3.0).isInPolygon(bonbon))
    }

    @Test fun `point outside polygon is outside polygon`() {
        assertFalse(p(0.0, 11.0).isInPolygon(p(0.0, 0.0).createSquare(10.0)))
    }

    @Test fun `point outside polygon is outside polygon at 180th meridian`() {
        assertFalse(p(-169.0, 0.0).isInPolygon(p(180.0, 0.0).createSquare(10.0)))
    }

    @Test fun `polygon direction does not matter for point-in-polygon check`() {
        val square = p(0.0, 0.0).createSquare(10.0).reversed()
        assertTrue(p(5.0, 5.0).isInPolygon(square))
    }

    @Test fun `polygon direction does not matter for point-in-polygon check at 180th meridian`() {
        val square = p(180.0, 0.0).createSquare(10.0).reversed()
        assertTrue(p(-175.0, 5.0).isInPolygon(square))
    }

    @Test fun `point in hole of concave polygon is outside polygon`() {
        val r = p(0.0, 0.0).createRhombusWithHoleAround()
        assertFalse(p(0.0, 0.0).isInPolygon(r))
        assertFalse(p(0.0, 0.5).isInPolygon(r))
    }

    @Test fun `point in hole of concave polygon is outside polygon at 180th meridian`() {
        val r = p(180.0, 0.0).createRhombusWithHoleAround()
        assertFalse(p(180.0, 0.0).isInPolygon(r))
        assertFalse(p(180.0, 0.5).isInPolygon(r))
    }

    @Test fun `point in shell of concave polygon is inside polygon`() {
        val r = p(0.0, 0.0).createRhombusWithHoleAround()
        assertTrue(p(0.75, 0.75).isInPolygon(r))
        assertTrue(p(1.5, 0.0).isInPolygon(r))
    }

    @Test fun `point in shell of concave polygon is inside polygon at 180th meridian`() {
        val r = p(180.0, 0.0).createRhombusWithHoleAround()
        assertTrue(p(-179.25, 0.75).isInPolygon(r))
        assertTrue(p(-178.5, 0.0).isInPolygon(r))
    }

    /* +++++++++++++++++++++++++++++ test point in multipolygon check +++++++++++++++++++++++++++ */

    @Test(expected = IllegalArgumentException::class)
    fun `isRingDefinedClockwise for empty list fails`() {
        emptyList<LatLon>().isRingDefinedClockwise()
    }

    @Test fun isRingDefinedClockwise() {
        val polygon = p(0.0, 0.0).createRhombus(1.0)
        assertFalse(polygon.isRingDefinedClockwise())
        assertTrue(polygon.reversed().isRingDefinedClockwise())
    }

    @Test fun `isRingDefinedClockwise for ring on 180th meridian`() {
        val polygon = p(180.0, 0.0).createRhombus(1.0)
        assertFalse(polygon.isRingDefinedClockwise())
        assertTrue(polygon.reversed().isRingDefinedClockwise())
    }

    @Test fun isInMultipolygon() {
        val origin = p(0.0, 0.0)

        val shellInHole = origin.createRhombus(1.0)
        val hole = origin.createRhombus(3.0).reversed()
        val shell = origin.createRhombus(5.0)
        val mp = listOf(shell, hole, shellInHole)

        assertTrue(origin.isInMultipolygon(mp))
        assertFalse(p(0.0, 2.0).isInMultipolygon(mp))
        assertTrue(p(0.0, 4.0).isInMultipolygon(mp))
        assertFalse(p(0.0, 6.0).isInMultipolygon(mp))
    }

    companion object {
        private val HH = p(10.0, 53.5)
    }
}

private fun p(x: Double, y: Double) = OsmLatLon(y, normalizeLongitude(x))
private val LatLon.x get() = longitude
private val LatLon.y get() = latitude

/*
  o---o
  | + |
  o---o
*/
private fun LatLon.createSquare(l: Double) = listOf(
    p(x + l, y + l),
    p(x + l, y - l),
    p(x - l, y - l),
    p(x - l, y + l),
    p(x + l, y + l)
)

/*
  o--o--o
  |     |
  o  .  o
  |     |
  o--o--o
*/
private fun LatLon.createSquareWithPointsAtCenterOfEdges(l: Double) = listOf(
    p(x + l, y + l),
    p(x + l, y),
    p(x + l, y - l),
    p(x, y - l),
    p(x - l, y - l),
    p(x - l, y),
    p(x - l, y + l),
    p(x, y + l),
    p(x + l, y + l)
)

/*
     o
   ╱  ╲
  o  +   o
   ╲  ╱
     o
*/
private fun LatLon.createRhombus(l: Double) = listOf(
    p(x, y + l),
    p(x + l, y),
    p(x, y - l),
    p(x - l, y),
    p(x, y + l)
)

/*
    ╱|
  ╱   ╲__
  ‾‾╲   ╱
     |╱
*/
private fun LatLon.createBonbon() = listOf(
    p(x, y + 2),
    p(x, y + 1),
    p(x + 1, y),
    p(x + 2, y),
    p(x, y - 2),
    p(x, y - 1),
    p(x - 1, y),
    p(x - 2, y),
    p(x, y + 2)
)

/*
     ╱╲
   ╱╱╲ ╲
   ╲╲__|╱
     ╲╱
*/
private fun LatLon.createRhombusWithHoleAround() = listOf(
    p(x, y + 1),
    p(x + 1, y),
    p(x + 1, y - 1),
    p(x + 2, y),
    p(x, y + 2),
    p(x - 2, y),
    p(x, y - 2),
    p(x + 1, y - 1),
    p(x, y - 1),
    p(x - 1, y),
    p(x, y + 1)
)
