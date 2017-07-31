package de.westnordost.streetcomplete.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class SphericalEarthMathTest extends TestCase
{
	private static LatLon HH = new OsmLatLon(53.5,10.0);

	public void testToBerlin()
	{
		checkHamburgTo(52.4, 13.4, 259, 117, 110);
	}

	public void testToLübeck()
	{
		checkHamburgTo(53.85, 10.68, 59, 49, 61);
	}

	public void testToLosAngeles()
	{
		checkHamburgTo(34, -118, 9075, 319, 206);
	}

	public void testToReykjavik()
	{
		checkHamburgTo(64.11, -21.98, 2152, 316, 280);
	}

	public void testToPortElizabeth()
	{
		checkHamburgTo(-33.9, -25.6, 10307, 209, 231);
	}

	public void testToPoles()
	{
		checkHamburgTo(90.0, 123.0, 4059, 0, null);
		checkHamburgTo(-90.0, 0.0, 15956, 180, null);
	}

	public void testToOtherSideOfEarth()
	{
		checkHamburgTo(-53.5, -170.0, (int) (Math.PI*6371), 270, 180);
	}

	public void testShortDistance()
	{
		LatLon one = new OsmLatLon(53.5712482, 9.9782365);
		LatLon two = new OsmLatLon(53.5712528, 9.9782517);
		assertEquals(1, (int) SphericalEarthMath.distance(one, two));
	}

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

	private void checkHamburgTo(double lat, double lon, int dist, int angle, Integer angle2)
	{
		LatLon t = new OsmLatLon(lat, lon);

		assertEquals(dist, Math.round(SphericalEarthMath.distance(HH, t) / 1000));
		assertEquals(dist, Math.round(SphericalEarthMath.distance(t, HH) / 1000));

		assertEquals(angle, Math.round(SphericalEarthMath.bearing(HH, t)));
		if(angle2 != null)
			assertEquals((int) angle2, Math.round(SphericalEarthMath.finalBearing(HH, t)));

	}

}
