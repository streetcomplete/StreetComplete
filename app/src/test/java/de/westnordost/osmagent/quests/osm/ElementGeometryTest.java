package de.westnordost.osmagent.quests.osm;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class ElementGeometryTest extends TestCase
{
	public void testFindCenterOfPolygons()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		List<LatLon> square = new ArrayList<>();
		square.add(new OsmLatLon(0,0));
		square.add(new OsmLatLon(20,0));
		square.add(new OsmLatLon(20,10));
		square.add(new OsmLatLon(0,10));
		polygons.add(square);
		ElementGeometry geom = new ElementGeometry(null, polygons);
		assertEquals(new OsmLatLon(10,5), geom.center);
	}

	public void testFindCenterOfPolyline()
	{
		List<List<LatLon>> polylines = new ArrayList<>();
		List<LatLon> polyline = new ArrayList<>();
		polyline.add(new OsmLatLon(0,0));
		polyline.add(new OsmLatLon(1,2));
		polyline.add(new OsmLatLon(2,4));
		polyline.add(new OsmLatLon(3,6));
		polyline.add(new OsmLatLon(10,20));
		polylines.add(polyline);
		ElementGeometry geom = new ElementGeometry(polylines, null);
		assertEquals(new OsmLatLon(5,10), geom.center);

	}
}
