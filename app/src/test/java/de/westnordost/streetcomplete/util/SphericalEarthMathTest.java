package de.westnordost.streetcomplete.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static org.assertj.core.api.Assertions.assertThat;

public class SphericalEarthMathTest extends TestCase
{
	private static LatLon HH = new OsmLatLon(53.5,10.0);

	/* ++++++++++++++++++++++++++++++++ test distance functions +++++++++++++++++++++++++++++++++ */

	public void testDistanceToBerlin()
	{
		checkHamburgTo(52.4, 13.4, 259, 117, 110);
	}

	public void testDistanceToLübeck()
	{
		checkHamburgTo(53.85, 10.68, 59, 49, 61);
	}

	public void testDistanceToLosAngeles()
	{
		checkHamburgTo(34, -118, 9075, 319, 206);
	}

	public void testDistanceToReykjavik()
	{
		checkHamburgTo(64.11, -21.98, 2152, 316, 280);
	}

	public void testDistanceToPortElizabeth()
	{
		checkHamburgTo(-33.9, -25.6, 10307, 209, 231);
	}

	public void testDistanceToPoles()
	{
		checkHamburgTo(90.0, 123.0, 4059, 0, null);
		checkHamburgTo(-90.0, 0.0, 15956, 180, null);
	}

	public void testDistanceToOtherSideOfEarth()
	{
		checkHamburgTo(-53.5, -170.0, (int) (Math.PI*6371), 270, 180);
	}

	public void testShortDistance()
	{
		LatLon one = new OsmLatLon(53.5712482, 9.9782365);
		LatLon two = new OsmLatLon(53.5712528, 9.9782517);
		assertEquals(1, (int) SphericalEarthMath.distance(one, two));
	}

	public void testDistanceOfPolylineIsZeroForOnePosition()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(new OsmLatLon(0,0));
		assertEquals(0.0, SphericalEarthMath.distance(positions));
	}

	public void testDistanceOfPolylineForTwoPositions()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = new OsmLatLon(0,0);
		LatLon p1 = new OsmLatLon(1,1);
		positions.add(p0);
		positions.add(p1);
		assertEquals(SphericalEarthMath.distance(p0,p1), SphericalEarthMath.distance(positions));
	}

	public void testDistanceOfPolylineForThreePositions()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = new OsmLatLon(0,0);
		LatLon p1 = new OsmLatLon(1,1);
		LatLon p2 = new OsmLatLon(2,2);
		positions.addAll(Arrays.asList(p0,p1,p2));
		assertEquals(
			SphericalEarthMath.distance(p0,p1) + SphericalEarthMath.distance(p1,p2),
			SphericalEarthMath.distance(positions)
		);
	}

	private void checkHamburgTo(double lat, double lon, int dist, int angle, Integer angle2)
	{
		LatLon t = new OsmLatLon(lat, lon);

		assertEquals(dist, Math.round(SphericalEarthMath.distance(HH, t) / 1000));
		assertEquals(dist, Math.round(SphericalEarthMath.distance(t, HH) / 1000));

		assertEquals(angle, Math.round(SphericalEarthMath.bearing(HH, t)));
		if(angle2 != null)
			assertEquals((int) angle2, Math.round(SphericalEarthMath.finalBearing(HH, t)));

	}

	/* +++++++++++++++++++++++++++++ test creation of bounding boxes ++++++++++++++++++++++++++++ */

	public void testEnclosingBoundingBox()
	{
		LatLon pos = new OsmLatLon(0, 0);
		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(pos, 5000);

		int dist = (int) (Math.sqrt(2) * 5000);

		// all four corners of the bbox should be 'radius' away
		assertEquals(dist, Math.round(SphericalEarthMath.distance(pos, bbox.getMin())));
		assertEquals(dist, Math.round(SphericalEarthMath.distance(pos, bbox.getMax())));
		assertEquals(dist, Math.round(SphericalEarthMath.distance(pos, new OsmLatLon(bbox.getMinLatitude(), bbox.getMaxLongitude()))));
		assertEquals(dist, Math.round(SphericalEarthMath.distance(pos, new OsmLatLon(bbox.getMaxLatitude(), bbox.getMinLongitude()))));

		assertEquals(225, Math.round(SphericalEarthMath.bearing(pos, bbox.getMin())));
		assertEquals(45, Math.round(SphericalEarthMath.bearing(pos, bbox.getMax())));
	}

	public void testEnclosingBoundingBoxCrosses180thMeridian()
	{
		LatLon pos = new OsmLatLon(0, 180);
		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(pos, 5000);

		assertTrue(bbox.crosses180thMeridian());
	}

	public void testEnclosingBoundingBoxLineEmptyFails()
	{
		List<LatLon> positions = new ArrayList<>();
		try
		{
			SphericalEarthMath.enclosingBoundingBox(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	public void testEnclosingBoundingBoxLine()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(new OsmLatLon(-4, 0));
		positions.add(new OsmLatLon(12, 3));
		positions.add(new OsmLatLon(1, 16));
		positions.add(new OsmLatLon(0, -6));

		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(positions);
		assertEquals(-4.0, bbox.getMinLatitude());
		assertEquals(12.0, bbox.getMaxLatitude());
		assertEquals(16.0, bbox.getMaxLongitude());
		assertEquals(-6.0, bbox.getMinLongitude());
	}

	public void testEnclosingBoundingBoxLineCrosses180thMeridian()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(new OsmLatLon(10,160));
		positions.add(new OsmLatLon(0,-150));
		positions.add(new OsmLatLon(-10,180));

		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(positions);
		assertTrue(bbox.crosses180thMeridian());
		assertEquals(-10.0, bbox.getMinLatitude());
		assertEquals(10.0, bbox.getMaxLatitude());
		assertEquals(-150.0, bbox.getMaxLongitude());
		assertEquals(160.0, bbox.getMinLongitude());
	}

	/* ++++++++++++++++++++++++++++++ test translating of positions +++++++++++++++++++++++++++++ */

	public void testTranslateLatitudeNorth()
	{
		checkTranslate(1000, 0);
	}

	public void testTranslateLatitudeSouth()
	{
		checkTranslate(1000, 180);
	}

	public void testTranslateLatitudeWest()
	{
		checkTranslate(1000, 270);
	}

	public void testTranslateLatitudeEast()
	{
		checkTranslate(1000, 90);
	}

	public void testTranslateLatitudeNorthEast()
	{
		checkTranslate(1000, 45);
	}

	public void testTranslateLatitudeSouthEast()
	{
		checkTranslate(1000, 135);
	}

	public void testTranslateLatitudeSouthWest()
	{
		checkTranslate(1000, 225);
	}

	public void testTranslateLatitudeNorthWest()
	{
		checkTranslate(1000, 315);
	}

	public void testTranslateOverBoundaries()
	{
		// cross 180th meridian both ways
		checkTranslate(new OsmLatLon(0,179.9999999), 1000, 90);
		checkTranslate(new OsmLatLon(0,-179.9999999), 1000, 270);
		// cross north pole and come out on the other side
		// should come out at 45,-90
		int quarterOfEarth = (int) (Math.PI/2 * SphericalEarthMath.EARTH_RADIUS);
		checkTranslate(new OsmLatLon(+45, 90), quarterOfEarth, 0);
		// should come out at -45,-90
		checkTranslate(new OsmLatLon(-45, 90), quarterOfEarth, 180);
	}

	private void checkTranslate(LatLon one, int distance, int angle)
	{
		LatLon two = SphericalEarthMath.translate(one, distance, angle);

		assertEquals(distance, Math.round(SphericalEarthMath.distance(one, two)));
		assertEquals(angle, Math.round(SphericalEarthMath.bearing(one, two)));
	}

	private void checkTranslate(int distance, int angle)
	{
		LatLon one = new OsmLatLon(53.5712482, 9.9782365);
		checkTranslate(one, distance, angle);
	}

	/* +++++++++++++++++++++++++++++ test calculation of center line ++++++++++++++++++++++++++++ */

	public void testCenterLineForPointFails()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(new OsmLatLon(0,0));
		try
		{
			SphericalEarthMath.centerLineOfPolyline(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	public void testCenterLineOfPolylineWithZeroLength()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = new OsmLatLon(0,0);
		LatLon p1 = new OsmLatLon(0,0);
		LatLon p2 = new OsmLatLon(0,0);
		positions.addAll(Arrays.asList(p0,p1,p2));
		assertThat(SphericalEarthMath.centerLineOfPolyline(positions)).containsExactly(p0, p1);
	}

	public void testCenterLineOfLineIsThatLine()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = new OsmLatLon(0,0);
		LatLon p1 = new OsmLatLon(1,1);
		positions.addAll(Arrays.asList(p0,p1));
		assertThat(SphericalEarthMath.centerLineOfPolyline(positions)).containsExactly(p0, p1);
	}

	public void testCenterLineOfPolylineIsTheMiddleOne()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = new OsmLatLon(0,0);
		LatLon p1 = new OsmLatLon(1,1);
		LatLon p2 = new OsmLatLon(2,2);
		LatLon p3 = new OsmLatLon(3,3);
		positions.addAll(Arrays.asList(p0,p1,p2,p3));
		assertThat(SphericalEarthMath.centerLineOfPolyline(positions)).containsExactly(p1, p2);
	}

	public void testCenterLineOfPolylineIsNotMiddleOneBecauseItIsSoLong()
	{
		List<LatLon> positions = new ArrayList<>();
		LatLon p0 = new OsmLatLon(0,0);
		LatLon p1 = new OsmLatLon(10,10);
		LatLon p2 = new OsmLatLon(11,11);
		LatLon p3 = new OsmLatLon(12,12);
		positions.addAll(Arrays.asList(p0,p1,p2,p3));
		assertThat(SphericalEarthMath.centerLineOfPolyline(positions)).containsExactly(p0, p1);
	}

	/* +++++++++++++++++++++++++ test calculation of center point of line +++++++++++++++++++++++ */

	public void testCenterPointForEmptyPolyListFails()
	{
		List<LatLon> positions = new ArrayList<>();
		try
		{
			SphericalEarthMath.centerPointOfPolyline(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	public void testCenterOfPolylineWithZeroLength()
	{
		List<LatLon> polyline = new ArrayList<>();
		polyline.add(new OsmLatLon(20,20));
		polyline.add(new OsmLatLon(20,20));
		assertEquals(new OsmLatLon(20,20), SphericalEarthMath.centerPointOfPolyline(polyline));
	}

	public void testCenterOfLine()
	{
		List<LatLon> polyline = new ArrayList<>();
		LatLon pos0 = new OsmLatLon(80,-20);
		LatLon pos1 = new OsmLatLon(-60,20);
		polyline.add(pos0);
		polyline.add(pos1);

		double dist = SphericalEarthMath.distance(pos0, pos1);
		double bearing = SphericalEarthMath.bearing(pos0, pos1);
		LatLon expect = SphericalEarthMath.translate(pos0, dist / 2, bearing);

		assertEquals(expect, SphericalEarthMath.centerPointOfPolyline(polyline));
	}

	public void testCenterOfLineThatCrosses180thMeridian()
	{
		List<LatLon> polyline = new ArrayList<>();
		LatLon pos0 = new OsmLatLon(0,170);
		LatLon pos1 = new OsmLatLon(0,-150);
		polyline.add(pos0);
		polyline.add(pos1);

		assertEquals(new OsmLatLon(0,-170), SphericalEarthMath.centerPointOfPolyline(polyline));

		List<LatLon> polyline2 = new ArrayList<>();
		LatLon pos2 = new OsmLatLon(0,150);
		LatLon pos3 = new OsmLatLon(0,-170);
		polyline2.add(pos2);
		polyline2.add(pos3);

		assertEquals(new OsmLatLon(0,170), SphericalEarthMath.centerPointOfPolyline(polyline2));
	}

	/* +++++++++++++++++++++++ test calculation of center point of polygon ++++++++++++++++++++++ */

	public void testCenterPointForEmptyPolygonFails()
	{
		List<LatLon> positions = new ArrayList<>();
		try
		{
			SphericalEarthMath.centerPointOfPolygon(positions);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	public void testCenterOfPolygonWithNoAreaSimplyReturnsFirstPoint()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(new OsmLatLon(10,10));
		positions.add(new OsmLatLon(20,10));
		positions.add(new OsmLatLon(30,10));
		assertEquals(new OsmLatLon(10,10), SphericalEarthMath.centerPointOfPolygon(positions));
	}

	public void testCenterOfPolygonAtOrigin()
	{
		LatLon center = new OsmLatLon(0,0);
		assertEquals(center, SphericalEarthMath.centerPointOfPolygon(createRhombusAround(center, 10000, 10000)));
	}

	public void testCenterOfPolygonAt180thMeridian()
	{
		LatLon center = new OsmLatLon(0,179.9);
		assertEquals(center, SphericalEarthMath.centerPointOfPolygon(createRhombusAround(center, 10000, 10000)));
	}

	private static List<LatLon> createRhombusAround(LatLon origin, double offsetY, double offsetX)
	{
		List<LatLon> square = new ArrayList<>();
		square.add(SphericalEarthMath.translate(origin, offsetX, 90));
		square.add(SphericalEarthMath.translate(origin, offsetY, 180));
		square.add(SphericalEarthMath.translate(origin, offsetX, 270));
		square.add(SphericalEarthMath.translate(origin, offsetY, 0));
		square.add(SphericalEarthMath.translate(origin, offsetX, 90));
		return square;
	}

	/* +++++++++++++++++++++++++++++++ test point in polygon check ++++++++++++++++++++++++++++++ */

	public void testPointAtPolygonVertexIsInPolygon()
	{
		assertTrue(SphericalEarthMath.isInPolygon(new OsmLatLon(10,-10), createSquareAroundOrigin(10)));
	}

	public void testPointAtPolygonEdgeIsInPolygon()
	{
		assertTrue(SphericalEarthMath.isInPolygon(new OsmLatLon(10,0), createSquareAroundOrigin(10)));
	}

	public void testPointInPolygonIsInPolygonDuh()
	{
		assertTrue(SphericalEarthMath.isInPolygon(new OsmLatLon(5,5), createSquareAroundOrigin(10)));
	}

	public void testPointOutsidePolygonIsOutsidePolygonDuh()
	{
		assertFalse(SphericalEarthMath.isInPolygon(new OsmLatLon(11,0), createSquareAroundOrigin(10)));
	}

	public void testPolygonDirectionDoesNotMatter()
	{
		List<LatLon> square = createSquareAroundOrigin(10);
		Collections.reverse(square);
		assertTrue(SphericalEarthMath.isInPolygon(new OsmLatLon(5,5), square));
	}

	private static List<LatLon> createSquareAroundOrigin(double len)
	{
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(len,len));
		square.add(new OsmLatLon(-len,len));
		square.add(new OsmLatLon(-len,-len));
		square.add(new OsmLatLon(len,-len));
		square.add(new OsmLatLon(len,len));
		return square;
	}

	/* +++++++++++++++++++++++++++++ test point in multipolygon check +++++++++++++++++++++++++++ */

	public void testEmptyListDefinedClockwiseFails()
	{
		try
		{
			SphericalEarthMath.isRingDefinedClockwise(Collections.emptyList());
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	public void testListDefinedClockwise()
	{
		List<LatLon> polygon = createRhombusAround(new OsmLatLon(0,0), 1000, 1000);
		assertFalse(SphericalEarthMath.isRingDefinedClockwise(polygon));
		Collections.reverse(polygon);
		assertTrue(SphericalEarthMath.isRingDefinedClockwise(polygon));
	}

	public void testListDefinedClockwiseOn180thMeridian()
	{
		List<LatLon> polygon = createRhombusAround(new OsmLatLon(0,180), 1000, 1000);
		assertFalse(SphericalEarthMath.isRingDefinedClockwise(polygon));
		Collections.reverse(polygon);
		assertTrue(SphericalEarthMath.isRingDefinedClockwise(polygon));
	}

	public void testPointInMultipolygon()
	{
		LatLon origin = new OsmLatLon(0,0);

		List<LatLon> shell = createRhombusAround(origin, 1000, 1000);
		List<LatLon> hole = createRhombusAround(origin, 500, 500);
		Collections.reverse(hole);
		List<LatLon> shellinhole = createRhombusAround(origin, 100, 100);
		List<List<LatLon>> mp = new ArrayList<>();
		mp.add(shell);
		mp.add(hole);
		mp.add(shellinhole);

		assertTrue(SphericalEarthMath.isInMultipolygon(origin, mp));
		assertFalse(SphericalEarthMath.isInMultipolygon(SphericalEarthMath.translate(origin, 400, 0), mp));
		assertTrue(SphericalEarthMath.isInMultipolygon(SphericalEarthMath.translate(origin, 600, 0), mp));
		assertFalse(SphericalEarthMath.isInMultipolygon(SphericalEarthMath.translate(origin, 1200, 0), mp));
	}
}
