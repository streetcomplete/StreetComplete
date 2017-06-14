package de.westnordost.streetcomplete.data.meta;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

import java.util.Map;

public class GeoJsonReaderTest extends TestCase
{
	public void testPoint()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Point\",\n" +
				"  \"coordinates\": [1,2]\n" +
				"}");
		assertTrue(g instanceof Point);
		Point p = (Point) g;
		assertEquals(1.0,p.getX());
		assertEquals(2.0,p.getY());
	}

	public void test3DPoint()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Point\",\n" +
				"  \"coordinates\": [1,2,3]\n" +
				"}");
		assertTrue(g instanceof Point);
		Point p = (Point) g;
		assertEquals(1.0,p.getX());
		assertEquals(2.0,p.getY());
		assertEquals(3.0,p.getCoordinate().z);
	}

	public void testLineString()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"LineString\",\n" +
				"  \"coordinates\": [[1,2],[2,4]]\n" +
				"}");
		assertTrue(g instanceof LineString);
		LineString l = (LineString) g;
		assertEquals(2,l.getNumPoints());
		assertEquals(1.0,l.getCoordinateN(0).x);
		assertEquals(2.0,l.getCoordinateN(0).y);
		assertEquals(2.0,l.getCoordinateN(1).x);
		assertEquals(4.0,l.getCoordinateN(1).y);
	}

	public void testMultiPoint()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiPoint\",\n" +
				"  \"coordinates\": [[1,2],[2,4]]\n" +
				"}");
		assertTrue(g instanceof MultiPoint);
		MultiPoint m = (MultiPoint) g;
		assertEquals(2,m.getNumGeometries());
		Point p0 = (Point) m.getGeometryN(0);
		Point p1 = (Point) m.getGeometryN(1);
		assertEquals(1.0,p0.getX());
		assertEquals(2.0,p0.getY());
		assertEquals(2.0,p1.getX());
		assertEquals(4.0,p1.getY());
	}

	public void testValidateLineString()
	{
		try
		{
			Geometry g = read("{\n" +
					"  \"type\": \"LineString\",\n" +
					"  \"coordinates\": [[1,2]]\n" +
					"}");
			fail();
		}
		catch (GeoJsonException e) {}
	}

	public void testMultiLineString()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiLineString\",\n" +
				"  \"coordinates\": [[[0,0],[4,0],[0,4]],[[1,1],[1,2],[2,1]]]\n" +
				"}");

		assertTrue(g instanceof MultiLineString);
		MultiLineString ml = (MultiLineString) g;
		assertEquals(2,ml.getNumGeometries());
		assertEquals(6,ml.getNumPoints());
	}

	public void testPolygon()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Polygon\",\n" +
				"  \"coordinates\": [[[0,0],[4,0],[0,4],[0,0]],[[1,1],[1,2],[2,1],[1,1]]]\n" +
				"}");

		assertTrue(g instanceof Polygon);
		Polygon p = (Polygon) g;
		assertEquals(8,p.getNumPoints());
		assertEquals(1,p.getNumInteriorRing());
	}

	public void testPolygonWithMergableInnerHoles()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Polygon\",\n" +
				"  \"coordinates\": [[[0,0],[4,0],[0,4],[4,4],[0,0]],[[1,1],[1,3],[3,3],[1,1]],[[1,1],[3,1],[3,3],[1,1]]]\n" +
				"}");

		assertTrue(g instanceof Polygon);
		Polygon p = (Polygon) g;
		assertEquals(10,p.getNumPoints());
		assertEquals(1,p.getNumInteriorRing());
	}

	public void testValidatePolygon()
	{
		try
		{
			Geometry g = read("{\n" +
					"  \"type\": \"Polygon\",\n" +
					"  \"coordinates\": [[[0,0],[4,0],[0,0]]]\n" +
					"}");
			fail();
		}
		catch (GeoJsonException e) {}

		try
		{
			Geometry g = read("{\n" +
					"  \"type\": \"Polygon\",\n" +
					"  \"coordinates\": [[[0,0],[4,0],[0,4],[2,3]]]\n" +
					"}");
			fail();
		}
		catch (GeoJsonException e) {}
	}

	public void testMultiPolygon()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiPolygon\",\n" +
				"  \"coordinates\": [[[[0,0],[4,0],[0,4],[0,0]]],[[[5,5],[3,2],[2,3],[5,5]]]]\n" +
				"}");

		assertTrue(g instanceof MultiPolygon);
		MultiPolygon mp = (MultiPolygon) g;
		assertEquals(2,mp.getNumGeometries());
		assertEquals(8,mp.getNumPoints());
	}

	public void testMultiPolygonMergable()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiPolygon\",\n" +
				"  \"coordinates\": [[[[0,0],[4,0],[0,4],[0,0]]], [[[4,0],[4,4],[0,4],[4,0]]], [[[4,0],[4,4],[8,4],[4,0]]]]\n" +
				"}");

		assertTrue(g instanceof MultiPolygon);
		MultiPolygon mp = (MultiPolygon) g;
		assertEquals(1,mp.getNumGeometries());
		assertEquals(6,mp.getNumPoints());
	}

	public void testEmptyGeometryCollection()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"GeometryCollection\",\n" +
				"  \"geometries\": []\n" +
				"}");
		assertTrue(g instanceof GeometryCollection);
		assertTrue(g.isEmpty());
	}

	public void testGeometryCollection()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"GeometryCollection\",\n" +
				"  \"geometries\":\n" +
				"  [\n" +
				"    {\n" +
				"      \"type\": \"Point\",\n" +
				"      \"coordinates\": [5,10]\n" +
				"    },\n" +
				"    {\n" +
				"      \"type\": \"LineString\",\n" +
				"      \"coordinates\": [[5,10],[10,5]]\n" +
				"    }\n" +
				"  ]\n" +
				"}");
		assertTrue(g instanceof GeometryCollection);
		assertEquals(2,g.getNumGeometries());
		assertTrue(g.getGeometryN(0) instanceof Point);
		assertTrue(g.getGeometryN(1) instanceof LineString);
		assertEquals(3,g.getNumPoints());
	}

	public void testFeatureNoProperties()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Feature\",\n" +
				"  \"properties\": {},\n" +
				"  \"geometry\": {\n" +
				"    \"type\": \"Point\",\n" +
				"    \"coordinates\": [5,5]\n" +
				"  }\n" +
				"}");
		assertTrue(g instanceof Point);
		assertNull(g.getUserData());
	}

	public void testFeatureWithProperties()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Feature\",\n" +
				"  \"properties\": {\"a\": \"jo\", \"b\": \"blub\"},\n" +
				"  \"geometry\": {\n" +
				"    \"type\": \"Point\",\n" +
				"    \"coordinates\": [5,5]\n" +
				"  }\n" +
				"}");
		assertTrue(g instanceof Point);
		assertTrue(g.getUserData() instanceof Map);
		Map<String,String> props = (Map) g.getUserData();
		assertEquals("jo",props.get("a"));
		assertEquals("blub",props.get("b"));
	}

	public void testEmptyFeatureCollection()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"FeatureCollection\",\n" +
				"  \"features\": []\n" +
				"}");
		assertTrue(g instanceof GeometryCollection);
		assertTrue(g.isEmpty());
	}

	public void testFeatures()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"FeatureCollection\",\n" +
				"  \"features\": [\n" +
				"      {\n" +
				"      \"type\": \"Feature\",\n" +
				"      \"properties\": {\"a\":\"b\"},\n" +
				"      \"geometry\": {\n" +
				"        \"type\": \"Point\",\n" +
				"        \"coordinates\": [10,20]\n" +
				"      }\n" +
				"    },\n" +
				"    {\n" +
				"      \"type\": \"Feature\",\n" +
				"      \"properties\": {\"c\":\"d\"},\n" +
				"      \"geometry\": {\n" +
				"        \"type\": \"LineString\",\n" +
				"        \"coordinates\": [[20,10],[30,30]]\n" +
				"      }\n" +
				"    }\n" +
				"  ]\n" +
				"}");

		assertTrue(g instanceof GeometryCollection);
		assertEquals(2,g.getNumGeometries());
		assertTrue(g.getGeometryN(0) instanceof Point);
		assertTrue(g.getGeometryN(1) instanceof LineString);
		assertEquals(3,g.getNumPoints());
		assertEquals("b",((Map)g.getGeometryN(0).getUserData()).get("a"));
		assertEquals("d",((Map)g.getGeometryN(1).getUserData()).get("c"));
	}

	private static Geometry read(String s)
	{
		return new GeoJsonReader().read(s);
	}
}
