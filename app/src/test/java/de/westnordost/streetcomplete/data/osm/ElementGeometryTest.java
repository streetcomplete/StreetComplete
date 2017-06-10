package de.westnordost.streetcomplete.data.osm;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;

public class ElementGeometryTest extends TestCase
{
	public void testFindCenterOfPolygons()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		polygons.add(createSquareAroundOrigin(5,5));
		ElementGeometry geom = new ElementGeometry(null, polygons);
		assertEquals(new OsmLatLon(0,0), geom.center);
	}

	public void testBoundsWithSquare()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		polygons.add(createSquareAroundOrigin(5,10));
		ElementGeometry geom = new ElementGeometry(null, polygons);
		BoundingBox expected = new BoundingBox(-5,-10,5,10);
		assertEquals(expected, geom.getBounds());
	}

	public void testFindCenterOfPolygonsWithHole()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		List<LatLon> hole = createSquareAroundOrigin(3,3);
		Collections.reverse(hole);
		polygons.add(createSquareAroundOrigin(5,5));
		polygons.add(hole);
		ElementGeometry geom = new ElementGeometry(null, polygons);
		double lat = geom.center.getLatitude();
		double lon = geom.center.getLongitude();
		assertTrue(
				Math.abs(lat) >= 3 && Math.abs(lat) <= 5 ||
				Math.abs(lon) >= 3 && Math.abs(lon) <= 5);
	}

	private static List<LatLon> createSquareAroundOrigin(double offsetLat, double offsetLon)
	{
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(-offsetLat,-offsetLon));
		square.add(new OsmLatLon(+offsetLat,-offsetLon));
		square.add(new OsmLatLon(+offsetLat,+offsetLon));
		square.add(new OsmLatLon(-offsetLat,+offsetLon));
		square.add(new OsmLatLon(-offsetLat,-offsetLon));
		return square;
	}

	public void testFindCenterOfPolygonWithNoArea()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(10,10));
		polygons.add(square);
		ElementGeometry geom = new ElementGeometry(null, polygons);
		assertEquals(null, geom.center);
	}

	public void testFindCenterOfPolyline()
	{
		List<List<LatLon>> polylines = new ArrayList<>();
		List<LatLon> polyline = new ArrayList<>();

		LatLon start = new OsmLatLon(-10,-20);
		LatLon finish = new OsmLatLon(10,20);

		polyline.add(start);
		polyline.add(finish);
		polylines.add(polyline);
		ElementGeometry geom = new ElementGeometry(polylines, null);

		double dist = SphericalEarthMath.distance(start, finish);
		double bearing = SphericalEarthMath.bearing(start, finish);
		LatLon expect = SphericalEarthMath.translate(start, dist / 2, bearing);

		assertEquals(expect, geom.center);
		assertEquals(new BoundingBox(start, finish), geom.getBounds());
	}

	public void testFindCenterOfPolylineWithZeroLength()
	{
		List<List<LatLon>> polylines = new ArrayList<>();
		List<LatLon> polyline = new ArrayList<>();
		polyline.add(new OsmLatLon(20,20));
		polyline.add(new OsmLatLon(20,20));
		polylines.add(polyline);
		ElementGeometry geom = new ElementGeometry(polylines, null);
		assertEquals(null, geom.center);
	}
}
