package de.westnordost.streetcomplete.data.osm;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class ElementGeometryTest extends TestCase
{
	public void testFindCenterOfPolygons()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(-5,-5));
		square.add(new OsmLatLon(+5,-5));
		square.add(new OsmLatLon(+5,+5));
		square.add(new OsmLatLon(-5,+5));
		polygons.add(square);
		ElementGeometry geom = new ElementGeometry(null, polygons);
		assertEquals(new OsmLatLon(0,0), geom.center);
	}

	public void testFindCenterOfPolygonsIgnoresHoles()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(-5,-5));
		square.add(new OsmLatLon(+5,-5));
		square.add(new OsmLatLon(+5,+5));
		square.add(new OsmLatLon(-5,+5));
		List<LatLon> hole = new ArrayList<>();
		hole.add(new OsmLatLon(1,1));
		hole.add(new OsmLatLon(1,3));
		hole.add(new OsmLatLon(3,2));
		polygons.add(square);
		polygons.add(hole);
		ElementGeometry geom = new ElementGeometry(null, polygons);
		assertEquals(new OsmLatLon(0,0), geom.center);
	}

	public void testFindCenterOfPolygonWithNoArea()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(10,10));
		square.add(new OsmLatLon(10,10));
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
	}

	public void testFindCenterOfPolylineWithZeroLength()
	{
		List<List<LatLon>> polylines = new ArrayList<>();
		List<LatLon> polyline = new ArrayList<>();
		polyline.add(new OsmLatLon(20,20));
		polyline.add(new OsmLatLon(20,20));
		polylines.add(polyline);
		ElementGeometry geom = new ElementGeometry(polylines, null);
		assertEquals(new OsmLatLon(20,20), geom.center);
	}
}
