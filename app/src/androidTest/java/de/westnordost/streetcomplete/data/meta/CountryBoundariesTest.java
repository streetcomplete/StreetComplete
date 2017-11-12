package de.westnordost.streetcomplete.data.meta;

import com.vividsolutions.jts.geom.GeometryCollection;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

import de.westnordost.osmapi.map.data.BoundingBox;

public class CountryBoundariesTest extends TestCase
{
	private static final String testGeoJson = "" +
			"{\"type\":\"FeatureCollection\",\"features\":[\n" +
			"{\"type\":\"Feature\",\"properties\":{\"ISO3166-1:alpha2\":\"AA\"},\"geometry\":{\n" +
			"\"type\":\"MultiPolygon\",\"coordinates\":[[[[-50,60],[-40,70],[-20,60],[-50,60]]],[[[-70,50],[0,50],[0,-20],[-70,-20],[-70,50]]]]}},\n" +
			"{\"type\":\"Feature\",\"properties\":{\"ISO3166-2\":\"AA-DD\"},\"geometry\":{\n" +
			"\"type\":\"Polygon\",\"coordinates\":[[[-40,30],[-20,30],[-20,6],[-40,6],[-40,30]]]}},\n" +
			"{\"type\":\"Feature\",\"properties\":{\"ISO3166-1:alpha2\":\"CC\",\"ISO3166-2\":\"AA-CC\"},\"geometry\":{\n" +
			"\"type\":\"Polygon\",\"coordinates\":[[[-50,40],[-10,40],[-10,0],[-50,0],[-50,40]]]}},\n" +
			"{\"type\":\"Feature\",\"properties\":{\"ISO3166-1:alpha2\":\"BB\"},\"geometry\":{\n" +
			"\"type\":\"Polygon\",\"coordinates\":[[[60,60],[100,60],[100,20],[60,20],[60,60]]]}}\n" +
			"]}";

	private CountryBoundaries countryBoundaries;

	public void setUp() throws Exception
	{
		super.setUp();
		countryBoundaries = new CountryBoundaries(
				(GeometryCollection) new GeoJsonReader().read(testGeoJson));
	}

	public void testIsIn()
	{
		assertTrue(countryBoundaries.isIn("BB",80,40));
		assertFalse(countryBoundaries.isIn("BB",0,0));

		assertTrue(countryBoundaries.isIn("AA",-30,3));
		assertTrue(countryBoundaries.isIn("CC",-30,3));
		assertTrue(countryBoundaries.isIn("AA-CC",-30,3));

		assertTrue(countryBoundaries.isIn("AA",-30,-10));
		assertFalse(countryBoundaries.isIn("AA-CC",-30,-10));

		assertTrue(countryBoundaries.isIn("AA",-40,63));
	}

	public void testIntersectsWith()
	{
		// outside
		assertFalse(countryBoundaries.intersectsWith("BB", new BoundingBox(0,40,80,55)));
		assertFalse(countryBoundaries.intersectsWith("BB", new BoundingBox(0,40,15,110)));
		assertFalse(countryBoundaries.intersectsWith("BB", new BoundingBox(80,55,90,110)));
		assertFalse(countryBoundaries.intersectsWith("BB", new BoundingBox(15,110,90,120)));

		// completely inside
		assertTrue(countryBoundaries.intersectsWith("BB", new BoundingBox(30,70,50,90)));

		// partly inside (classical intersection)
		assertTrue(countryBoundaries.intersectsWith("BB", new BoundingBox(0,0,30,70)));
	}

	public void testIntersectsWithAny()
	{
		assertTrue(countryBoundaries.intersectsWithAny(
				new String[] {"AA-DD", "BB"}, new BoundingBox(0,0,30,70)));
	}

	public void testIsInAny()
	{
		assertTrue(countryBoundaries.isInAny(new String[]{"BB","AA-DD"},-30,20));
		assertFalse(countryBoundaries.isInAny(new String[]{"BB","AA-DD"},-30,-10));
	}

	public void testBoundaries()
	{
		assertEquals(Arrays.asList("AA-DD","CC","AA"), countryBoundaries.getIsoCodes(-30,20));
		assertEquals(Arrays.asList("BB"), countryBoundaries.getIsoCodes(80,40));
		assertEquals(Arrays.asList("AA"), countryBoundaries.getIsoCodes(-40,63));
		assertEquals(Collections.emptyList(), countryBoundaries.getIsoCodes(20,20));
	}


}
