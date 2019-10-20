package de.westnordost.streetcomplete.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static de.westnordost.streetcomplete.util.SphericalEarthMath.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class SphericalEarthMathTest
{
	private static LatLon HH = p(10.0, 53.5);

	/* ++++++++++++++++++++++++++++++++ test distance functions +++++++++++++++++++++++++++++++++ */

	@Test public void distanceToBerlin()
	{
		checkHamburgTo(52.4, 13.4, 259, 117, 120);
	}

	@Test public void distanceToLübeck()
	{
		checkHamburgTo(53.85, 10.68, 59, 49, 49);
	}

	@Test public void distanceToLosAngeles()
	{
		checkHamburgTo(34, -118, 9075, 319, 208);
	}

	@Test public void distanceToReykjavik()
	{
		checkHamburgTo(64.11, -21.98, 2152, 316, 288);
	}

	@Test public void distanceToPortElizabeth()
	{
		checkHamburgTo(-33.9, -25.6, 10307, 209, 200);
	}

	@Test public void distanceToPoles()
	{
		checkHamburgTo(90.0, 123.0, 4059, 0, null);
		checkHamburgTo(-90.0, 0.0, 15956, 180, null);
	}

	@Test public void distanceToOtherSideOfEarth()
	{
		checkHamburgTo(-53.5, -170.0, (int) (Math.PI*6371), 270, 270);
	}

	@Test public void shortDistance()
	{
		LatLon one = p(9.9782365, 53.5712482);
		LatLon two = p(9.9782517, 53.5712528);
		assertEquals(1, (int) distance(one, two));
	}

	@Test public void distanceOfPolylineIsZeroForOnePosition()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(p(0, 0));
		assertEquals(0.0, distance(positions), 0);
	}

	@Test public void distanceOfPolylineForTwoPositions()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = p(0, 0);
		LatLon p1 = p(1, 1);
		positions.add(p0);
		positions.add(p1);
		assertEquals(distance(p0,p1), distance(positions), 0);
	}

	@Test public void distanceOfPolylineForThreePositions()
	{
		LatLon p0 = p(0, 0);
		LatLon p1 = p(1, 1);
		LatLon p2 = p(2, 2);
		List<LatLon> positions = new ArrayList<>(Arrays.asList(p0, p1, p2));
		assertEquals(
			distance(p0,p1) + distance(p1,p2),
			distance(positions),
			1e-16
		);
	}

	private void checkHamburgTo(double lat, double lon, int dist, int angle, Integer angle2)
	{
		LatLon t = p(lon, lat);

		assertEquals(dist, Math.round(distance(HH, t) / 1000));
		assertEquals(dist, Math.round(distance(t, HH) / 1000));

		assertEquals(angle, Math.round(bearing(HH, t)));
		if(angle2 != null)
			assertEquals((int) angle2, Math.round(finalBearing(HH, t)));

	}

	/* ++++++++++++++++++++++++++++++ test distance to arc distance +++++++++++++++++++++++++++++ */

	@Test public void simpleDistanceToHorizontalArc()
	{
		LatLon start = new OsmLatLon(0.0, -0.01);
		LatLon end = new OsmLatLon(0.0, +0.01);
		LatLon point = new OsmLatLon(0.01, 0.0);
		LatLon intersect = new OsmLatLon(0.0, 0.0);
		assertEquals(distance(point, intersect), crossTrackDistance(start, end, point), 0.01);
		assertEquals(distance(start, intersect), alongTrackDistance(start, end, point), 0.01);
	}

	@Test public void simpleDistanceToVerticalArc()
	{
		LatLon start = new OsmLatLon(-0.01, 0.0);
		LatLon end = new OsmLatLon(+0.01, 0.0);
		LatLon point = new OsmLatLon(0.0, 0.01);
		LatLon intersect = new OsmLatLon(0.0, 0.0);
		assertEquals(distance(point, intersect), crossTrackDistance(start, end, point), 0.01);
		assertEquals(distance(start, intersect), alongTrackDistance(start, end, point), 0.01);
	}

	@Test public void simpleDistanceToSlopedArc()
	{
		LatLon start = new OsmLatLon(-0.01, -0.01);
		LatLon end = new OsmLatLon(+0.01, +0.01);
		LatLon point = new OsmLatLon(-0.01, +0.01);
		LatLon intersect = new OsmLatLon(0.0, 0.0);
		assertEquals(distance(point, intersect), crossTrackDistance(start, end, point), 0.01);
		assertEquals(distance(start, intersect), alongTrackDistance(start, end, point), 0.01);
	}

	@Test public void distanceToHorizontalArcCrossing180thMeridian()
	{
		LatLon start = new OsmLatLon(0.0, 170);
		LatLon end = new OsmLatLon(0.0, -170);
		LatLon point = new OsmLatLon(0.01, -175.0);
		LatLon intersect = new OsmLatLon(0.0, -175.0);
		assertEquals(distance(point, intersect), crossTrackDistance(start, end, point), 0.01);
		assertEquals(distance(start, intersect), alongTrackDistance(start, end, point), 0.01);
	}

	@Test public void distanceToVerticalArcCrossingNorthPole()
	{
		LatLon start = new OsmLatLon(80.0, 0.0);
		LatLon end = new OsmLatLon(0.0, 180.0);
		LatLon point = new OsmLatLon(85.0, 179.99);
		LatLon intersect = new OsmLatLon(85.0, 180.0);
		assertEquals(distance(point, intersect), crossTrackDistance(start, end, point), 0.01);
		assertEquals(distance(start, intersect), alongTrackDistance(start, end, point), 0.01);
	}

	@Test public void distanceToSinglePosition()
	{
		LatLon point = new OsmLatLon(0.01, 0.0);
		LatLon intersect = new OsmLatLon(0.0, 0.0);
		assertEquals(
			distance(intersect, point),
			crossTrackDistance(Collections.singletonList(intersect), point),
			0.01
		);
	}

	@Test public void distanceToSingleArc()
	{
		LatLon start = new OsmLatLon(0.0, -0.01);
		LatLon end = new OsmLatLon(0.0, +0.01);
		LatLon point = new OsmLatLon(0.01, 0.0);
		assertEquals(
			crossTrackDistance(start, end, point),
			crossTrackDistance(Arrays.asList(start, end), point),
			0.01
		);
	}

	@Test public void distanceToMultipleArcs()
	{
		LatLon p0 = new OsmLatLon(0.0, -0.01);
		LatLon p1 = new OsmLatLon(0.0, +0.01);
		LatLon p2 = new OsmLatLon(0.0, +0.02);
		LatLon point = new OsmLatLon(0.01, 0.0);
		assertEquals(
			crossTrackDistance(p0, p1, point),
			crossTrackDistance(Arrays.asList(p0, p1, p2), point),
			0.01
		);
		assertEquals(
			crossTrackDistance(p0, p1, point),
			crossTrackDistance(Arrays.asList(p2, p1, p0), point),
			0.01
		);
	}

	/* +++++++++++++++++++++++++++++ test creation of bounding boxes ++++++++++++++++++++++++++++ */

	@Test public void enclosingBoundingBoxRadius()
	{
		LatLon pos = p(0, 0);
		BoundingBox bbox = enclosingBoundingBox(pos, 5000);

		int dist = (int) (Math.sqrt(2) * 5000);

		// all four corners of the bbox should be 'radius' away
		assertEquals(dist, Math.round(distance(pos, bbox.getMin())));
		assertEquals(dist, Math.round(distance(pos, bbox.getMax())));
		assertEquals(dist, Math.round(distance(pos, p(bbox.getMaxLongitude(), bbox.getMinLatitude()))));
		assertEquals(dist, Math.round(distance(pos, p(bbox.getMinLongitude(), bbox.getMaxLatitude()))));

		assertEquals(225, Math.round(bearing(pos, bbox.getMin())));
		assertEquals(45, Math.round(bearing(pos, bbox.getMax())));
	}

	@Test public void enclosingBoundingBoxCrosses180thMeridian()
	{
		LatLon pos = p(180, 0);
		BoundingBox bbox = enclosingBoundingBox(pos, 5000);

		assertTrue(bbox.crosses180thMeridian());
	}

	@Test public void enclosingBoundingBoxLineEmptyFails()
	{
		List<LatLon> positions = new ArrayList<>();
		try
		{
			enclosingBoundingBox(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	@Test public void enclosingBoundingBoxLine()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(p(0, -4));
		positions.add(p(3, 12));
		positions.add(p(16, 1));
		positions.add(p(-6, 0));

		BoundingBox bbox = enclosingBoundingBox(positions);
		assertEquals(-4.0, bbox.getMinLatitude(),0);
		assertEquals(12.0, bbox.getMaxLatitude(),0);
		assertEquals(16.0, bbox.getMaxLongitude(),0);
		assertEquals(-6.0, bbox.getMinLongitude(),0);
	}

	@Test public void enclosingBoundingBoxLineCrosses180thMeridian()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(p(160, 10));
		positions.add(p(-150, 0));
		positions.add(p(180, -10));

		BoundingBox bbox = enclosingBoundingBox(positions);
		assertTrue(bbox.crosses180thMeridian());
		assertEquals(-10.0, bbox.getMinLatitude(),0);
		assertEquals(10.0, bbox.getMaxLatitude(),0);
		assertEquals(-150.0, bbox.getMaxLongitude(),0);
		assertEquals(160.0, bbox.getMinLongitude(),0);
	}

	/* ++++++++++++++++++++++++++++++ test translating of positions +++++++++++++++++++++++++++++ */

	@Test public void translateLatitudeNorth()
	{
		checkTranslate(1000, 0);
	}

	@Test public void translateLatitudeSouth()
	{
		checkTranslate(1000, 180);
	}

	@Test public void translateLatitudeWest()
	{
		checkTranslate(1000, 270);
	}

	@Test public void translateLatitudeEast()
	{
		checkTranslate(1000, 90);
	}

	@Test public void translateLatitudeNorthEast()
	{
		checkTranslate(1000, 45);
	}

	@Test public void translateLatitudeSouthEast()
	{
		checkTranslate(1000, 135);
	}

	@Test public void translateLatitudeSouthWest()
	{
		checkTranslate(1000, 225);
	}

	@Test public void translateLatitudeNorthWest()
	{
		checkTranslate(1000, 315);
	}

	@Test public void translateOverBoundaries()
	{
		// cross 180th meridian both ways
		checkTranslate(p(179.9999999, 0), 1000, 90);
		checkTranslate(p(-179.9999999, 0), 1000, 270);
		// cross north pole and come out on the other side
		// should come out at 45,-90
		int quarterOfEarth = (int) (Math.PI/2 * EARTH_RADIUS);
		checkTranslate(p(90, +45), quarterOfEarth, 0);
		// should come out at -45,-90
		checkTranslate(p(90, -45), quarterOfEarth, 180);
	}

	private void checkTranslate(LatLon one, int distance, int angle)
	{
		LatLon two = translate(one, distance, angle);

		assertEquals(distance, Math.round(distance(one, two)));
		assertEquals(angle, Math.round(bearing(one, two)));
	}

	private void checkTranslate(int distance, int angle)
	{
		LatLon one = p(9.9782365, 53.5712482);
		checkTranslate(one, distance, angle);
	}

	/* +++++++++++++++++++++++++++++ test calculation of center line ++++++++++++++++++++++++++++ */

	@Test public void centerLineForPointFails()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(p(0, 0));
		try
		{
			centerLineOfPolyline(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	@Test public void centerLineOfPolylineWithZeroLength()
	{
		LatLon p0 = p(0, 0);
		LatLon p1 = p(0, 0);
		LatLon p2 = p(0, 0);
		List<LatLon> positions = new ArrayList<>(Arrays.asList(p0, p1, p2));
		assertThat(centerLineOfPolyline(positions)).containsExactly(p0, p1);
	}

	@Test public void centerLineOfLineIsThatLine()
	{
		LatLon p0 = p(0, 0);
		LatLon p1 = p(1, 1);
		List<LatLon> positions = new ArrayList<>(Arrays.asList(p0, p1));
		assertThat(centerLineOfPolyline(positions)).containsExactly(p0, p1);
	}

	@Test public void centerLineOfPolylineIsTheMiddleOne()
	{
		LatLon p0 = p(0, 0);
		LatLon p1 = p(1, 1);
		LatLon p2 = p(2, 2);
		LatLon p3 = p(3, 3);
		List<LatLon> positions = new ArrayList<>(Arrays.asList(p0, p1, p2, p3));
		assertThat(centerLineOfPolyline(positions)).containsExactly(p1, p2);
	}

	@Test public void centerLineOfPolylineIsNotMiddleOneBecauseItIsSoLong()
	{
		LatLon p0 = p(0, 0);
		LatLon p1 = p(10, 10);
		LatLon p2 = p(11, 11);
		LatLon p3 = p(12, 12);
		List<LatLon> positions = new ArrayList<>(Arrays.asList(p0, p1, p2, p3));
		assertThat(centerLineOfPolyline(positions)).containsExactly(p0, p1);
	}

	/* +++++++++++++++++++++++++ test calculation of center point of line +++++++++++++++++++++++ */

	@Test public void centerPointForEmptyPolyListFails()
	{
		List<LatLon> positions = new ArrayList<>();
		try
		{
			centerPointOfPolyline(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	@Test public void centerOfPolylineWithZeroLength()
	{
		List<LatLon> polyline = new ArrayList<>();
		polyline.add(p(20, 20));
		polyline.add(p(20, 20));
		assertEquals(p(20, 20), centerPointOfPolyline(polyline));
	}

	@Test public void centerOfLine()
	{
		List<LatLon> polyline = new ArrayList<>();
		LatLon pos0 = p(-20, 80);
		LatLon pos1 = p(20, -60);
		polyline.add(pos0);
		polyline.add(pos1);

		assertEquals(p(0, 10), centerPointOfPolyline(polyline));
	}

	@Test public void centerOfLineThatCrosses180thMeridian()
	{
		List<LatLon> polyline = new ArrayList<>();
		LatLon pos0 = p(170, 0);
		LatLon pos1 = p(-150, 0);
		polyline.add(pos0);
		polyline.add(pos1);

		assertEquals(p(-170, 0), centerPointOfPolyline(polyline));

		List<LatLon> polyline2 = new ArrayList<>();
		LatLon pos2 = p(150, 0);
		LatLon pos3 = p(-170, 0);
		polyline2.add(pos2);
		polyline2.add(pos3);

		assertEquals(p(170, 0), centerPointOfPolyline(polyline2));
	}

	/* +++++++++++++++++++++++ test calculation of center point of polygon ++++++++++++++++++++++ */

	@Test public void centerPointForEmptyPolygonFails()
	{
		List<LatLon> positions = new ArrayList<>();
		try
		{
			centerPointOfPolygon(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	@Test public void centerOfPolygonWithNoAreaSimplyReturnsFirstPoint()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(p(10, 10));
		positions.add(p(10, 20));
		positions.add(p(10, 30));
		assertEquals(p(10, 10), centerPointOfPolygon(positions));
	}

	@Test public void centerOfPolygonAtOrigin()
	{
		ShorthandLatLon center = p(0, 0);
		assertEquals(center, centerPointOfPolygon(createRhombusAround(center, 1)));
	}

	@Test public void centerOfPolygonAt180thMeridian()
	{
		ShorthandLatLon center = p(179.9, 0);
		assertEquals(center, centerPointOfPolygon(createRhombusAround(center, 1)));
	}

	/* +++++++++++++++++++++++++ test calculation of point in line string +++++++++++++++++++++++ */

	@Test public void pointOnLine()
	{
		List<LatLon> list = Arrays.asList(p(0,0), p(10,0));
		assertEquals(p(2.5,0), pointOnPolylineFromStart(list, distance(list) * 0.25));
	}

	@Test public void pointOnPolyline()
	{
		List<LatLon> list = Arrays.asList(p(0,0), p(5,0), p(10,0));
		assertEquals(p(2.5,0), pointOnPolylineFromStart(list, distance(list) * 0.25));
	}

	@Test public void pointOnLineWhichCrosses180thMeridian()
	{
		List<LatLon> list = Arrays.asList(p(179,0), p(-179,0));
		assertEquals(p(-180,0), pointOnPolylineFromStart(list, distance(list) * 0.5));
	}

	/* +++++++++++++++++++++++++++++++ test point in polygon check ++++++++++++++++++++++++++++++ */

	@Test public void pointAtPolygonVertexIsInPolygon()
	{
		List<LatLon> square = createSquareWithPointsAtCenterOfEdgesAround(p(0, 0),10);
		for(LatLon pos : square)
		{
			assertTrue(isInPolygon(pos, square));
		}
	}

	@Test public void pointAtPolygonVertexIsInPolygonAt180thMeridian()
	{
		List<LatLon> square = createSquareWithPointsAtCenterOfEdgesAround(p(180, 0),10);
		for(LatLon pos : square)
		{
			assertTrue(isInPolygon(pos, square));
		}
	}

	@Test public void pointAtPolygonEdgeIsInPolygon()
	{
		List<LatLon> square = createSquareAround(p(0, 0),10);
		assertTrue(isInPolygon(p(0, 10), square));
		assertTrue(isInPolygon(p(10, 0), square));
		assertTrue(isInPolygon(p(-10, 0), square));
		assertTrue(isInPolygon(p(0, -10), square));
	}

	@Test public void pointAtPolygonEdgeIsInPolygonAt180thMeridian()
	{
		List<LatLon> square = createSquareAround(p(180, 0),10);
		assertTrue(isInPolygon(p(180, 10), square));
		assertTrue(isInPolygon(p(-170, 0), square));
		assertTrue(isInPolygon(p(170, 0), square));
		assertTrue(isInPolygon(p(180, -10), square));
	}

	@Test public void pointInPolygonIsInPolygon()
	{
		assertTrue(isInPolygon(p(0, 0), Arrays.asList(p(1, 1),p(1, -2),p(-2, 1))));
	}

	@Test public void pointInPolygonIsInPolygonAt180thMeridian()
	{
		assertTrue(isInPolygon(p(180, 0), Arrays.asList(p(-179, 1),p(-179, -2),p(178, 1))));
	}

	// The counting number algorithm in particular needs to handle a special case where the ray
	// intersects the polygon in a polygon vertex

	@Test public void pointInPolygonWhoseRayIntersectAVertexIsInPolygon()
	{
		assertTrue(isInPolygon(p(0, 0), createRhombusAround(p(0, 0),1)));
	}

	@Test public void pointInPolygonWhoseRayIntersectAVertexIsInPolygonAt180thMeridian()
	{
		assertTrue(isInPolygon(p(180, 0), createRhombusAround(p(180, 0),1)));
	}

	@Test public void pointOutsidePolygonWhoseRayIntersectAVertexIsOutsidePolygon()
	{
		List<LatLon> rhombus = createRhombusAround(p(0, 0),1);
		// four checks here because the ray could be cast in any direction
		assertFalse(isInPolygon(p(-2, 1), rhombus));
		assertFalse(isInPolygon(p(-2, 0), rhombus));
		assertFalse(isInPolygon(p(1, -2), rhombus));
		assertFalse(isInPolygon(p(0, -2), rhombus));
	}

	@Test public void pointOutsidePolygonWhoseRayIntersectAVertexIsOutsidePolygonAt180thMeridian()
	{
		List<LatLon> rhombus = createRhombusAround(p(180, 0),1);
		// four checks here because the ray could be cast in any direction
		assertFalse(isInPolygon(p(178, 1), rhombus));
		assertFalse(isInPolygon(p(178, 0), rhombus));
		assertFalse(isInPolygon(p(-179, -2), rhombus));
		assertFalse(isInPolygon(p(180, -2), rhombus));
	}

	@Test public void pointInPolygonWhoseRayIntersectsPolygonEdgesIsInsidePolygon()
	{
		List<LatLon> bonbon = createBonbonAround(p(0, 0));
		assertTrue(isInPolygon(p(0, 0), bonbon));
	}

	@Test public void pointInPolygonWhoseRayIntersectsPolygonEdgesIsInsidePolygonAt180thMeridian()
	{
		List<LatLon> bonbon = createBonbonAround(p(180, 0));
		assertTrue(isInPolygon(p(180, 0), bonbon));
	}

	@Test public void pointOutsidePolygonWhoseRayIntersectsPolygonEdgesIsOutsidePolygon()
	{
		List<LatLon> bonbon = createBonbonAround(p(0, 0));
		// four checks here because the ray could be cast in any direction
		assertFalse(isInPolygon(p(-3, 0), bonbon));
		assertFalse(isInPolygon(p(+3, 0), bonbon));
		assertFalse(isInPolygon(p(0, +3), bonbon));
		assertFalse(isInPolygon(p(0, -3), bonbon));
	}

	@Test public void pointOutsidePolygonWhoseRayIntersectsPolygonEdgesIsOutsidePolygonAt180thMeridian()
	{
		List<LatLon> bonbon = createBonbonAround(p(180, 0));
		// four checks here because the ray could be cast in any direction
		assertFalse(isInPolygon(p(177, 0), bonbon));
		assertFalse(isInPolygon(p(-177, 0), bonbon));
		assertFalse(isInPolygon(p(180, +3), bonbon));
		assertFalse(isInPolygon(p(180, -3), bonbon));
	}

	@Test public void pointOutsidePolygonIsOutsidePolygon()
	{
		assertFalse(isInPolygon(p(0, 11), createSquareAround(p(0, 0),10)));
	}

	@Test public void pointOutsidePolygonIsOutsidePolygonAt180thMeridian()
	{
		assertFalse(isInPolygon(p(-169, 0), createSquareAround(p(180, 0),10)));
	}

	@Test public void polygonDirectionDoesNotMatter()
	{
		List<LatLon> square = createSquareAround(p(0, 0),10);
		Collections.reverse(square);
		assertTrue(isInPolygon(p(5, 5), square));
	}

	@Test public void polygonDirectionDoesNotMatterAt180thMeridian()
	{
		List<LatLon> square = createSquareAround(p(180, 0),10);
		Collections.reverse(square);
		assertTrue(isInPolygon(p(-175, 5), square));
	}

	@Test public void pointInHoleOfConcavePolygonIsOutsidePolygon()
	{
		List<LatLon> r = createRhombusWithHoleAround(p(0, 0));
		assertFalse(isInPolygon(p(0, 0), r));
		assertFalse(isInPolygon(p(0, 0.5), r));
	}

	@Test public void pointInHoleOfConcavePolygonIsOutsidePolygonAt180thMeridian()
	{
		List<LatLon> r = createRhombusWithHoleAround(p(180, 0));
		assertFalse(isInPolygon(p(180, 0), r));
		assertFalse(isInPolygon(p(180, 0.5), r));
	}

	@Test public void pointInShellOfConcavePolygonIsInsidePolygon()
	{
		List<LatLon> r = createRhombusWithHoleAround(p(0, 0));
		assertTrue(isInPolygon(p(0.75, 0.75), r));
		assertTrue(isInPolygon(p(1.5, 0), r));
	}

	@Test public void pointInShellOfConcavePolygonIsInsidePolygonAt180thMeridian()
	{
		List<LatLon> r = createRhombusWithHoleAround(p(180, 0));
		assertTrue(isInPolygon(p(-179.25, 0.75), r));
		assertTrue(isInPolygon(p(-178.5, 0), r));
	}

	/* +++++++++++++++++++++++++++++ test point in multipolygon check +++++++++++++++++++++++++++ */

	@Test public void emptyListDefinedClockwiseFails()
	{
		try
		{
			isRingDefinedClockwise(Collections.emptyList());
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	@Test public void listDefinedClockwise()
	{
		List<LatLon> polygon = createRhombusAround(p(0, 0), 1);
		assertFalse(isRingDefinedClockwise(polygon));
		Collections.reverse(polygon);
		assertTrue(isRingDefinedClockwise(polygon));
	}

	@Test public void listDefinedClockwiseOn180thMeridian()
	{
		List<LatLon> polygon = createRhombusAround(p(180, 0), 1);
		assertFalse(isRingDefinedClockwise(polygon));
		Collections.reverse(polygon);
		assertTrue(isRingDefinedClockwise(polygon));
	}

	@Test public void pointInMultipolygon()
	{
		ShorthandLatLon origin = p(0, 0);

		List<LatLon> shell = createRhombusAround(origin, 1);
		List<LatLon> hole = createRhombusAround(origin, 3);
		Collections.reverse(hole);
		List<LatLon> shellinhole = createRhombusAround(origin, 5);
		List<List<LatLon>> mp = new ArrayList<>();
		mp.add(shell);
		mp.add(hole);
		mp.add(shellinhole);

		assertTrue(isInMultipolygon(origin, mp));
		assertFalse(isInMultipolygon(p(0,2), mp));
		assertTrue(isInMultipolygon(p(0,4), mp));
		assertFalse(isInMultipolygon(p(0,6), mp));
	}

	/* ------------------------------------------------------------------------------------------ */

	/*
			o---o
			| + |
			o---o
	 */
	private static List<LatLon> createSquareAround(ShorthandLatLon origin, double l)
	{
		return Arrays.asList(
			p(origin.x+l, origin.y+l),
			p(origin.x+l, origin.y-l),
			p(origin.x-l, origin.y-l),
			p(origin.x-l, origin.y+l),
			p(origin.x+l, origin.y+l)
		);
	}

	/*
			o--o--o
			|     |
			o  .  o
			|     |
			o--o--o
	*/
	private static List<LatLon> createSquareWithPointsAtCenterOfEdgesAround(ShorthandLatLon o, double l)
	{
		return Arrays.asList(
			p(o.x+l, o.y+l),
			p(o.x+l, o.y),
			p(o.x+l, o.y-l),
			p(o.x, o.y-l),
			p(o.x-l, o.y-l),
			p(o.x-l, o.y),
			p(o.x-l, o.y+l),
			p(o.x, o.y+l),
			p(o.x+l, o.y+l)
		);
	}

	/*
			   o
			 ╱  ╲
			o  +   o
			 ╲  ╱
			   o
	*/
	private static List<LatLon> createRhombusAround(ShorthandLatLon o, double l)
	{
		return Arrays.asList(
			p(o.x,   o.y+l),
			p(o.x+l, o.y),
			p(o.x,   o.y-l),
			p(o.x-l, o.y),
			p(o.x,   o.y+l)
		);
	}

	/*
	  ╱|
	╱   ╲__
	‾‾╲   ╱
	   |╱
	 */
	private static List<LatLon> createBonbonAround(ShorthandLatLon o)
	{
		return Arrays.asList(
			p(o.x,   o.y+2),
			p(o.x,   o.y+1),
			p(o.x+1, o.y),
			p(o.x+2, o.y),
			p(o.x,   o.y-2),
			p(o.x,   o.y-1),
			p(o.x-1, o.y),
			p(o.x-2, o.y),
			p(o.x,   o.y+2)
		);
	}

	/*
		   ╱╲
		 ╱╱╲ ╲
		 ╲╲__|╱
		   ╲╱
	*/
	private static List<LatLon> createRhombusWithHoleAround(ShorthandLatLon o)
	{
		return Arrays.asList(
			p(o.x,   o.y+1),
			p(o.x+1, o.y),
			p(o.x+1, o.y-1),
			p(o.x+2, o.y),
			p(o.x,   o.y+2),
			p(o.x-2, o.y),
			p(o.x,   o.y-2),
			p(o.x+1, o.y-1),
			p(o.x,   o.y-1),
			p(o.x-1, o.y),
			p(o.x,   o.y+1)
		);
	}

	private static ShorthandLatLon p(double lon, double lat)
	{
		return new ShorthandLatLon(lon, lat);
	}

	private static class ShorthandLatLon implements LatLon
	{
		public ShorthandLatLon(double x, double y) { this.x = normalizeLongitude(x); this.y = y;}
		final double y,x;
		@Override public double getLatitude() { return y; }
		@Override public double getLongitude() { return x; }
		@Override public boolean equals(Object obj)
		{
			if(obj instanceof LatLon)
			{
				LatLon o = (LatLon) obj;
				return o.getLatitude() == getLatitude() && o.getLongitude() == getLongitude();
			}
			return false;
		}
	}
}
