package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.util.ktx.equalsInOsm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SphericalEarthMathTest {

    //region LatLon extension functions

    //region distanceTo, initialBearingTo, finalBearingTo

    @Test fun `distance to Berlin`() {
        checkHamburgTo(52.4, 13.4, 259, 117, 120)
    }

    @Test fun `distance to Luebeck`() {
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

    //endregion

    //region isRightOf

    @Test fun `point right of line`() {
        val px = p(0.0, 0.0)
        val p0 = p(-1.0, 0.0)

        assertFalse(px.isRightOf(p0, 269.0))
        assertTrue(px.isRightOf(p0, 270.0))
        assertTrue(px.isRightOf(p0, 0.0))
        assertTrue(px.isRightOf(p0, 45.0))
        assertTrue(px.isRightOf(p0, 89.0))
        assertFalse(px.isRightOf(p0, 90.0))
    }

    @Test fun `point right of two lines that make a right-turn`() {
        val p0 = p(-2.0, 0.0)
        val p1 = p(0.0, 0.0)
        val p2 = p(1.0, -1.0)

        assertTrue(p(0.0, -1.0).isRightOf(p0, p1, p2))
        assertFalse(p(2.0, -1.0).isRightOf(p0, p1, p2))
        assertFalse(p(-2.0, 1.0).isRightOf(p0, p1, p2))
        assertFalse(p(1.0, 1.0).isRightOf(p0, p1, p2))
    }

    @Test fun `point right of two lines that make a left-turn`() {
        val p0 = p(-2.0, 0.0)
        val p1 = p(0.0, 0.0)
        val p2 = p(0.0, 2.0)

        assertTrue(p(0.0, -1.0).isRightOf(p0, p1, p2))
        assertTrue(p(2.0, -1.0).isRightOf(p0, p1, p2))
        assertFalse(p(-2.0, 1.0).isRightOf(p0, p1, p2))
        assertTrue(p(1.0, 1.0).isRightOf(p0, p1, p2))
    }

    //endregion

    //region translate

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

    //endregion

    //region distanceToArc, crossTrackDistanceTo, alongTrackDistanceTo, distanceToArc, distanceToArcs

    @Test fun `simple distance to horizontal arc`() {
        val start = p(-0.01, 0.0)
        val end = p(+0.01, 0.0)
        val point = p(0.0, -0.01)
        val intersect = p(0.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.distanceToArc(start, end), 0.01)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `simple distance to vertical arc`() {
        val start = p(0.0, -0.01)
        val end = p(0.0, +0.01)
        val point = p(0.01, 0.0)
        val intersect = p(0.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.distanceToArc(start, end), 0.01)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `simple distance to sloped arc`() {
        val start = p(-0.01, -0.01)
        val end = p(+0.01, +0.01)
        val point = p(+0.01, -0.01)
        val intersect = p(0.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.distanceToArc(start, end), 0.01)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance of point not orthogonal but before arc`() {
        val start = p(+0.01, 0.0)
        val end = p(+0.02, 0.0)
        val point = p(0.0, -0.01)
        val intersect = p(0.0, 0.0)
        assertEquals(point.distanceTo(start), point.distanceToArc(start, end), 0.01)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), -point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance of point not orthogonal but after arc`() {
        val start = p(-0.02, 0.0)
        val end = p(-0.01, 0.0)
        val point = p(0.0, -0.01)
        val intersect = p(0.0, 0.0)
        assertEquals(point.distanceTo(end), point.distanceToArc(start, end), 0.01)
        assertEquals(point.distanceTo(intersect), point.crossTrackDistanceTo(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.02)
    }

    @Test fun `distance to horizontal arc crossing 180th meridian`() {
        val start = p(170.0, 0.0)
        val end = p(-170.0, 0.0)
        val point = p(-175.0, 0.01)
        val intersect = p(-175.0, 0.0)
        assertEquals(point.distanceTo(intersect), point.distanceToArc(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance to vertical arc crossing north pole`() {
        val start = p(0.0, 80.0)
        val end = p(180.0, 0.0)
        val point = p(179.99, 85.0)
        val intersect = p(180.0, 85.0)
        assertEquals(point.distanceTo(intersect), point.distanceToArc(start, end), 0.01)
        assertEquals(start.distanceTo(intersect), point.alongTrackDistanceTo(start, end), 0.01)
    }

    @Test fun `distance to single position`() {
        val point = p(0.01, 0.0)
        val intersect = p(0.0, 0.0)
        assertEquals(
            point.distanceTo(intersect),
            point.distanceToArcs(listOf(intersect)),
            0.01
        )
    }

    @Test fun `distance to single arc`() {
        val start = p(0.0, -0.01)
        val end = p(0.0, +0.01)
        val point = p(0.01, 0.0)
        assertEquals(
            point.distanceToArc(start, end),
            point.distanceToArcs(listOf(start, end)),
            0.01
        )
    }

    @Test fun `distance to multiple arcs`() {
        val p0 = p(0.0, -0.01)
        val p1 = p(0.0, +0.01)
        val p2 = p(0.0, +0.02)
        val point = p(0.01, 0.0)
        assertEquals(
            point.distanceToArc(p0, p1),
            point.distanceToArcs(listOf(p0, p1, p2)),
            0.01
        )
        assertEquals(
            point.distanceToArc(p0, p1),
            point.distanceToArcs(listOf(p2, p1, p0)),
            0.01
        )
    }

    //endregion

    //region nearestPointOnArc, nearestPointOnArcs

    @Test fun `nearestPointOnArc returns startpoint`() {
        assertEquals(
            p(1.0, 1.0),
            p(0.0, 0.0).nearestPointOnArc(p(1.0, 1.0), p(2.0, 1.0))
        )
    }

    @Test fun `nearestPointOnArc returns endpoint`() {
        assertEquals(
            p(1.0, 1.0),
            p(0.0, 0.0).nearestPointOnArc(p(2.0, 1.0), p(1.0, 1.0))
        )
    }

    @Test fun `nearestPointOnArc returns a point on the arc`() {
        assertTrue(
            p(0.5, 0.5).equalsInOsm(p(0.0, 0.0).nearestPointOnArc(p(1.0, 0.0), p(0.0, 1.0)))
        )
    }

    //endregion

    //region enclosingBoundingBox

    @Test fun `enclosingbbox radius`() {
        val pos = p(0.0, 0.0)
        val bbox = pos.enclosingBoundingBox(5000.0)

        val dist = (sqrt(2.0) * 5000).toInt()

        // all four corners of the bbox should be 'radius' away
        assertEquals(dist, pos.distanceTo(bbox.min).roundToInt())
        assertEquals(dist, pos.distanceTo(bbox.max).roundToInt())
        assertEquals(
            dist,
            pos.distanceTo(p(bbox.max.longitude, bbox.min.latitude)).roundToInt()
        )
        assertEquals(
            dist,
            pos.distanceTo(p(bbox.min.longitude, bbox.max.latitude)).roundToInt()
        )

        assertEquals(225, pos.initialBearingTo(bbox.min).roundToInt())
        assertEquals(45, pos.initialBearingTo(bbox.max).roundToInt())
    }

    @Test fun `enclosingbbox crosses 180th meridian`() {
        val bbox = p(180.0, 0.0).enclosingBoundingBox(5000.0)

        assertTrue(bbox.crosses180thMeridian)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `enclosingbbox fails for empty line`() {
        listOf<LatLon>().enclosingBoundingBox()
    }

    @Test fun `enclosingbbox for points`() {
        val positions = listOf(p(0.0, -4.0), p(3.0, 12.0), p(16.0, 1.0), p(-6.0, 0.0))
        val bbox = positions.enclosingBoundingBox()
        assertEquals(
            BoundingBox(-4.0, -6.0, 12.0, 16.0),
            bbox
        )
    }

    @Test fun `enclosingbbox for points difficult to represent exactly with floating point numbers`() {
        val p1 = p(0.1, 0.2)
        val p2 = p(0.4, 0.1)
        val bbox = listOf(p1, p2).enclosingBoundingBox()
        assertEquals(
            BoundingBox(0.1, 0.1, 0.2, 0.4),
            bbox
        )
    }

    @Test fun `enclosingbbox for line crosses 180th meridian`() {
        val positions = listOf(p(160.0, 10.0), p(-150.0, 0.0), p(180.0, -10.0))
        val bbox = positions.enclosingBoundingBox()
        assertTrue(bbox.crosses180thMeridian)
        assertEquals(-10.0, bbox.min.latitude, 0.0)
        assertEquals(10.0, bbox.max.latitude, 0.0)
        assertEquals(-150.0, bbox.max.longitude, 0.0)
        assertEquals(160.0, bbox.min.longitude, 0.0)
    }

    //endregion

    //endregion

    //region Polyline extension functions

    //region centerLineOfPolyline

    @Test(expected = IllegalArgumentException::class)
    fun `centerLineOfPolyline for point fails`() {
        listOf(p(0.0, 0.0)).centerLineOfPolyline()
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

    //endregion

    //region centerPointOfPolyline

    @Test(expected = IllegalArgumentException::class)
    fun `centerPointOfPolyline fails for empty list`() {
        listOf<LatLon>().centerPointOfPolyline()
    }

    @Test fun `centerPointOfPolyline for line with zero length`() {
        val p0 = p(20.0, 20.0)
        assertEquals(p0, listOf(p0, p0).centerPointOfPolyline())
    }

    @Test fun `centerPointOfPolyline for simple line`() {
        val polyline = listOf(p(-20.0, 80.0), p(20.0, -60.0))
        assertEquals(p(0.0, 10.0), polyline.centerPointOfPolyline())
    }

    @Test fun `centerPointOfPolyline for line that crosses 180th meridian`() {
        assertEquals(p(-170.0, 0.0), listOf(p(170.0, 0.0), p(-150.0, 0.0)).centerPointOfPolyline())
        assertEquals(p(170.0, 0.0), listOf(p(150.0, 0.0), p(-170.0, 0.0)).centerPointOfPolyline())
    }

    //endregion

    //region pointOnPolylineFromStart, pointsOnPolylineFromStart, pointOnPolylineFromEnd, pointsOnPolylineFromEnd

    @Test fun `pointOnPolyline for single line`() {
        val list = listOf(p(0.0, 0.0), p(10.0, 0.0))
        assertEquals(p(2.5, 0.0), list.pointOnPolylineFromStart(list.measuredLength() * 0.25))
        assertEquals(p(7.5, 0.0), list.pointOnPolylineFromEnd(list.measuredLength() * 0.25))
    }

    @Test fun `pointOnPolyline for polyline`() {
        val list = listOf(p(0.0, 0.0), p(5.0, 0.0), p(10.0, 0.0))
        assertEquals(p(2.5, 0.0), list.pointOnPolylineFromStart(list.measuredLength() * 0.25))
        assertEquals(p(7.5, 0.0), list.pointOnPolylineFromEnd(list.measuredLength() * 0.25))
    }

    @Test fun `pointOnPolyline for line that crosses 180th meridian`() {
        val list = listOf(p(179.0, 0.0), p(-179.0, 0.0))
        assertEquals(p(-180.0, 0.0), list.pointOnPolylineFromStart(list.measuredLength() * 0.5))
        assertEquals(p(-180.0, 0.0), list.pointOnPolylineFromEnd(list.measuredLength() * 0.5))
    }

    @Test fun `pointsOnPolyline for polyline`() {
        val list = listOf(p(0.0, 0.0), p(5.0, 0.0), p(10.0, 0.0))
        val points = listOf(
            list.measuredLength() * 0.25,
            list.measuredLength() * 0.75, // unsorted order
            list.measuredLength() * 0.55, // 0.55 amd 0.75 are in the same segment
        )

        assertEquals(
            listOf(p(2.5, 0.0), p(5.5, 0.0), p(7.5, 0.0)),
            list.pointsOnPolylineFromStart(points)
        )
        assertEquals(
            listOf(p(7.5, 0.0), p(4.5, 0.0), p(2.5, 0.0)),
            list.pointsOnPolylineFromEnd(points)
        )
    }

    @Test fun `pointsOnPolyline for no point`() {
        val list = listOf(p(0.0, 0.0), p(5.0, 0.0))
        assertEquals(
            listOf<LatLon>(),
            list.pointsOnPolylineFromStart(listOf())
        )
        assertEquals(
            listOf<LatLon>(),
            list.pointsOnPolylineFromEnd(listOf())
        )
    }

    //endregion

    //region isRingDefinedClockwise

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

    //endregion

    //region intersectsWith

    @Test(expected = IllegalArgumentException::class)
    fun `intersectsWith requires line`() {
        listOf(p(0.0, 0.0)).intersectsWith(listOf(p(1.0, 0.0), p(0.0, 1.0)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `intersectsWith requires line for the parameter too`() {
        listOf(p(1.0, 0.0), p(0.0, 1.0)).intersectsWith(listOf(p(0.0, 0.0)))
    }

    @Test fun `intersectsWith finds intersection`() {
        val h = listOf(p(-1.0, 0.0), p(1.0, 0.0))
        val v = listOf(p(0.0, -1.0), p(0.0, 1.0))
        assertTrue(h.intersectsWith(v))
    }

    @Test fun `intersectsWith does not count touching endpoints`() {
        val h = listOf(p(-1.0, 0.0), p(1.0, 0.0))
        val h2 = listOf(p(1.0, 0.0), p(2.0, 0.0))
        val h3 = listOf(p(-1.0, 0.0), p(-2.0, 0.0))
        assertFalse(h.intersectsWith(h2))
        assertFalse(h.intersectsWith(h3))
        assertFalse(h.intersectsWith(h2.reversed()))
        assertFalse(h.intersectsWith(h3.reversed()))
    }

    //endregion

    //region intersectionOf

    @Test fun `two lines intersect at endpoints`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(1.0, 0.0)
        val q1 = p(0.0, 1.0)
        val q2 = p(1.0, 0.0)

        val i = intersectionOf(p1, p2, q1, q2)!!
        assertEquals(p2.longitude, i.longitude, 1e-9)
        assertEquals(p2.latitude, i.latitude, 1e-9)
    }

    @Test fun `two lines intersect at start points`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(1.0, 0.0)
        val q1 = p(0.0, 0.0)
        val q2 = p(1.0, 1.0)

        val i = intersectionOf(p1, p2, q1, q2)!!
        assertEquals(p1.longitude, i.longitude, 1e-9)
        assertEquals(p1.latitude, i.latitude, 1e-9)
    }

    @Test fun `two lines intersect somewhere in the middle`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(2.0, 0.0)
        val q1 = p(0.0, 1.0)
        val q2 = p(2.0, -1.0)

        val i = intersectionOf(p1, p2, q1, q2)!!
        assertEquals(1.0, i.longitude, 1e-9)
        assertEquals(0.0, i.latitude, 1e-9)
    }

    @Test fun `two lines do not intersect somewhere after segment p`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(4.0, 0.0)
        val q1 = p(0.0, 2.0)
        val q2 = p(1.0, 1.0)

        assertNull(intersectionOf(p1, p2, q1, q2))
    }

    @Test fun `two lines do not intersect somewhere after segment q`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(1.0, 0.0)
        val q1 = p(0.0, 1.0)
        val q2 = p(4.0, -1.0)

        assertNull(intersectionOf(p1, p2, q1, q2))
    }

    @Test fun `two lines do not intersect somewhere before segment p`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(0.0, 1.0)
        val q1 = p(-2.0, -1.0)
        val q2 = p(0.0, 1.0)

        assertNull(intersectionOf(p1, p2, q1, q2))
    }

    @Test fun `two lines do not intersect somewhere before segment q`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(0.0, 4.0)
        val q1 = p(4.0, 1.0)
        val q2 = p(5.0, 2.0)

        assertNull(intersectionOf(p1, p2, q1, q2))
    }

    @Test fun `two lines intersect that are on the same great circle`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(2.0, 0.0)
        val q1 = p(4.0, 0.0)
        val q2 = p(-2.0, 0.0)

        assertEquals(p1, intersectionOf(p1, p2, q1, q2))
    }

    @Test fun `two lines do not intersect that are on the same great circle`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(1.0, 0.0)
        val q1 = p(3.0, 0.0)
        val q2 = p(2.0, 0.0)

        assertNull(intersectionOf(p1, p2, q1, q2))
    }

    @Test fun `two lines intersect on the other side of the earth`() {
        val p1 = p(0.0, 0.0)
        val p2 = p(180.0, 0.0)
        val q1 = p(-90.0, 1.0)
        val q2 = p(90.0, -1.0)

        val i = intersectionOf(p1, p2, q1, q2)!!
        assertEquals(0.0, i.longitude, 1e-9)
        assertEquals(0.0, i.latitude, 1e-9)
    }

    //endregion

    //endregion

    //region Polygon extension functions

    //region centerPointOfPolygon

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

    //endregion

    //region isInPolygon

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
        val square = p(0.0, 0.0).createCounterClockwiseSquare(10.0)
        assertTrue(p(0.0, 10.0).isInPolygon(square))
        assertTrue(p(10.0, 0.0).isInPolygon(square))
        assertTrue(p(-10.0, 0.0).isInPolygon(square))
        assertTrue(p(0.0, -10.0).isInPolygon(square))
    }

    @Test fun `point at polygon edge at 180th meridian is in polygon`() {
        val square = p(180.0, 0.0).createCounterClockwiseSquare(10.0)
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

    @Test fun `point in polygon whose ray intersects a vertex is in polygon`() {
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
        assertFalse(p(0.0, 11.0).isInPolygon(p(0.0, 0.0).createCounterClockwiseSquare(10.0)))
    }

    @Test fun `point outside polygon is outside polygon at 180th meridian`() {
        assertFalse(p(-169.0, 0.0).isInPolygon(p(180.0, 0.0).createCounterClockwiseSquare(10.0)))
    }

    @Test fun `polygon direction does not matter for point-in-polygon check`() {
        val square = p(0.0, 0.0).createCounterClockwiseSquare(10.0).reversed()
        assertTrue(p(5.0, 5.0).isInPolygon(square))
    }

    @Test fun `polygon direction does not matter for point-in-polygon check at 180th meridian`() {
        val square = p(180.0, 0.0).createCounterClockwiseSquare(10.0).reversed()
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

    //endregion

    //region isInMultipolygon

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

    @Test fun issue2064() {
        val way = createWay218917749()
        val p = p(13.4486174, 52.4758700)
        assertFalse(p.isInPolygon(way))
        assertFalse(p.isInMultipolygon(listOf(way)))
    }

    //endregion

    //region measuredArea

    @Test fun `polygon area is 0 for a polygon with less than 3 edges`() {
        val twoEdges = listOf(p(0.0, 0.0), p(1.0, 0.0), p(1.0, 1.0))
        assertEquals(0.0, twoEdges.measuredArea(), 0.0)
    }

    @Test fun `polygon area is 0 for a polygon that is not closed`() {
        val notClosed = listOf(p(0.0, 0.0), p(1.0, 0.0), p(1.0, 1.0), p(0.0, 1.0))
        assertEquals(0.0, notClosed.measuredArea(), 0.0)
    }

    @Test fun `polygon area is positive for a counterclockwise polygon`() {
        val square = p(0.0, 0.0).createCounterClockwiseSquare(1.0)
        assertFalse(square.isRingDefinedClockwise())
        assertTrue(square.measuredAreaSigned() > 0)
        assertTrue(square.measuredArea() > 0)
    }

    @Test fun `polygon area is negative for a clockwise polygon`() {
        val square = p(0.0, 0.0).createCounterClockwiseSquare(1.0).reversed()
        assertTrue(square.isRingDefinedClockwise())
        assertTrue(square.measuredAreaSigned() < 0)
        assertTrue(square.measuredArea() > 0)
    }

    //endregion

    //endregion

    //region Bounding Box extension functions

    @Test fun `contains works`() {
        val bbox = bbox(-1.0, -2.0, 1.0, 2.0)
        assertTrue(bbox.contains(p(-2.0, -1.0)))
        assertTrue(bbox.contains(p(+2.0, -1.0)))
        assertTrue(bbox.contains(p(-2.0, +1.0)))
        assertTrue(bbox.contains(p(+2.0, +1.0)))
        assertTrue(bbox.contains(p(+0.0, +0.0)))

        assertFalse(bbox.contains(p(-2.1, 0.0)))
        assertFalse(bbox.contains(p(+2.1, 0.0)))
        assertFalse(bbox.contains(p(0.0, -1.1)))
        assertFalse(bbox.contains(p(0.0, +1.1)))
    }

    @Test fun `contains works at 180th meridian`() {
        val bbox = bbox(0.0, 179.0, 1.0, -179.0)
        assertTrue(bbox.contains(p(179.0, 0.0)))
        assertTrue(bbox.contains(p(179.0, 1.0)))
        assertTrue(bbox.contains(p(180.0, 0.0)))
        assertTrue(bbox.contains(p(180.0, 1.0)))
        assertTrue(bbox.contains(p(-179.0, 0.0)))
        assertTrue(bbox.contains(p(-179.0, 1.0)))
        assertTrue(bbox.contains(p(-180.0, 0.0)))
        assertTrue(bbox.contains(p(-180.0, 1.0)))

        assertFalse(bbox.contains(p(0.0, 0.0)))
    }

    @Test fun `isCompletelyInside works`() {
        val bbox1 = bbox(-1.0, -1.0, 1.0, 1.0)
        val bbox2 = bbox(-0.5, -0.5, 0.5, 1.0)
        assertTrue(bbox2.isCompletelyInside(bbox1))
        assertFalse(bbox1.isCompletelyInside(bbox2))
    }

    @Test fun `isCompletelyInside works at 180th meridian`() {
        val bbox1 = bbox(0.0, 179.0, 1.0, -179.0)
        val bbox2 = bbox(0.0, 179.5, 0.5, -179.5)
        assertTrue(bbox2.isCompletelyInside(bbox1))
        assertFalse(bbox1.isCompletelyInside(bbox2))
    }

    @Test fun `enlargedBy really enlarges bounding box`() {
        val bbox = bbox(0.0, 0.0, 1.0, 1.0)
        assertTrue(bbox.isCompletelyInside(bbox.enlargedBy(1.0)))
    }

    @Test fun `enlargedBy really enlarges bounding box, even at 180th meridian`() {
        val bbox1 = bbox(0.0, 179.0, 1.0, 180.0)
        // enlarged bounding box should go over the 180th meridian
        assertTrue(bbox1.isCompletelyInside(bbox1.enlargedBy(100.0)))
        // here already bbox2 crosses the 180th meridian, maybe this makes a difference
        val bbox2 = bbox(0.0, 179.0, 1.0, -179.0)
        assertTrue(bbox2.isCompletelyInside(bbox2.enlargedBy(100.0)))
    }

    @Test fun `bounding box not on same latitude do not intersect`() {
        val bbox = bbox(0.0, 0.0, 1.0, 1.0)
        val above = bbox(1.1, 0.0, 1.2, 1.0)
        val below = bbox(-0.2, 0.0, -0.1, 1.0)
        assertFalse(bbox.intersect(above))
        assertFalse(bbox.intersect(below))
        assertFalse(above.intersect(bbox))
        assertFalse(below.intersect(bbox))
    }

    @Test fun `bounding box not on same longitude do not intersect`() {
        val bbox = bbox(0.0, 0.0, 1.0, 1.0)
        val left = bbox(0.0, -0.2, 1.0, -0.1)
        val right = bbox(0.0, 1.1, 1.0, 1.2)
        assertFalse(bbox.intersect(left))
        assertFalse(bbox.intersect(right))
        assertFalse(left.intersect(bbox))
        assertFalse(right.intersect(bbox))
    }

    @Test fun `intersecting bounding boxes`() {
        val bbox = bbox(0.0, 0.0, 1.0, 1.0)
        val touchLeft = bbox(0.0, -0.1, 1.0, 0.0)
        val touchUpperRightCorner = bbox(1.0, 1.0, 1.1, 1.1)
        val completelyInside = bbox(0.4, 0.4, 0.8, 0.8)
        val intersectLeft = bbox(0.4, -0.5, 0.5, 0.5)
        val intersectRight = bbox(0.4, 0.8, 0.5, 1.2)
        val intersectTop = bbox(0.9, 0.4, 1.1, 0.8)
        val intersectBottom = bbox(-0.2, 0.4, 0.2, 0.6)
        assertTrue(bbox.intersect(touchLeft))
        assertTrue(bbox.intersect(touchUpperRightCorner))
        assertTrue(bbox.intersect(completelyInside))
        assertTrue(bbox.intersect(intersectLeft))
        assertTrue(bbox.intersect(intersectRight))
        assertTrue(bbox.intersect(intersectTop))
        assertTrue(bbox.intersect(intersectBottom))
        // and the other way around
        assertTrue(touchLeft.intersect(bbox))
        assertTrue(touchUpperRightCorner.intersect(bbox))
        assertTrue(completelyInside.intersect(bbox))
        assertTrue(intersectLeft.intersect(bbox))
        assertTrue(intersectRight.intersect(bbox))
        assertTrue(intersectTop.intersect(bbox))
        assertTrue(intersectBottom.intersect(bbox))
    }

    @Test fun `bounding box not on same longitude do not intersect, even on 180th meridian`() {
        val bbox = bbox(0.0, 179.0, 1.0, -179.0)
        val other = bbox(0.0, -178.0, 1.0, 178.0)
        assertFalse(bbox.intersect(other))
        assertFalse(other.intersect(bbox))
    }

    @Test fun `intersecting bounding boxes at 180th meridian`() {
        val bbox = bbox(0.0, 179.5, 1.0, -170.0)
        val touchLeft = bbox(0.0, 179.0, 1.0, 180.0)
        val touchUpperRightCorner = bbox(1.0, -170.0, 1.1, -169.0)
        val completelyInside = bbox(0.4, 179.9, 0.8, -179.9)
        val intersectLeft = bbox(0.4, 179.0, 0.5, 179.9)
        val intersectRight = bbox(0.4, -179.0, 0.5, -150.0)
        val intersectTop = bbox(0.9, 179.9, 1.1, -179.8)
        val intersectBottom = bbox(-0.2, 179.9, 0.2, -179.8)
        assertTrue(bbox.intersect(touchLeft))
        assertTrue(bbox.intersect(touchUpperRightCorner))
        assertTrue(bbox.intersect(completelyInside))
        assertTrue(bbox.intersect(intersectLeft))
        assertTrue(bbox.intersect(intersectRight))
        assertTrue(bbox.intersect(intersectTop))
        assertTrue(bbox.intersect(intersectBottom))
        // and the other way around
        assertTrue(touchLeft.intersect(bbox))
        assertTrue(touchUpperRightCorner.intersect(bbox))
        assertTrue(completelyInside.intersect(bbox))
        assertTrue(intersectLeft.intersect(bbox))
        assertTrue(intersectRight.intersect(bbox))
        assertTrue(intersectTop.intersect(bbox))
        assertTrue(intersectBottom.intersect(bbox))
    }

    //endregion

    companion object {
        private val HH = p(10.0, 53.5)
    }
}

private fun p(x: Double, y: Double) = LatLon(y, normalizeLongitude(x))
private val LatLon.x get() = longitude
private val LatLon.y get() = latitude

/*
  o---o
  | + |
  o---o
*/
private fun LatLon.createCounterClockwiseSquare(l: Double) = listOf(
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
  o  + o
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
   ╲╲_|╱
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

private fun createWay218917749() = listOf(
    p(13.4488926, 52.4759134),
    p(13.4490121, 52.4758463),
    p(13.4489534, 52.4758081),
    p(13.4488763, 52.4757578),
    p(13.4488096, 52.4757548),
    p(13.4486691, 52.4758216),
    p(13.4487710, 52.4758997),
    p(13.4488293, 52.4758721),
    p(13.4488332, 52.4758709),
    p(13.4488373, 52.4758700),
    p(13.4488420, 52.4758699),
    p(13.4488466, 52.4758701),
    p(13.4488512, 52.4758709),
    p(13.4488553, 52.4758722),
    p(13.4488588, 52.4758740),
    p(13.4488616, 52.4758761),
    p(13.4488638, 52.4758784),
    p(13.4488651, 52.4758807),
    p(13.4488658, 52.4758831),
    p(13.4488662, 52.4758855),
    p(13.4488660, 52.4758879),
    p(13.4488652, 52.4758902),
    p(13.4488638, 52.4758926),
    p(13.4488621, 52.4758946),
    p(13.4488926, 52.4759134)
)
