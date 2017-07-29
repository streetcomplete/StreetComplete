package de.westnordost.streetcomplete.data.meta;

import com.vividsolutions.jts.geom.GeometryCollection;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

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

	public void setUp()
	{
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

	public void testIsInAny()
	{
		assertTrue(countryBoundaries.isInAny(Arrays.asList("BB","AA-DD"),-30,20));
		assertFalse(countryBoundaries.isInAny(Arrays.asList("BB","AA-DD"),-30,-10));
	}

	public void testBoundaries()
	{
		assertEquals(Arrays.asList("AA-DD","CC","AA"), countryBoundaries.getIsoCodes(-30,20));
		assertEquals(Arrays.asList("BB"), countryBoundaries.getIsoCodes(80,40));
		assertEquals(Arrays.asList("AA"), countryBoundaries.getIsoCodes(-40,63));
		assertEquals(Collections.emptyList(), countryBoundaries.getIsoCodes(20,20));
	}
}
