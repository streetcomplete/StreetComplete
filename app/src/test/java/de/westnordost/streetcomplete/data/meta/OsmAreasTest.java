package de.westnordost.streetcomplete.data.meta;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Way;

import static org.junit.Assert.*;

public class OsmAreasTest
{
	@Test public void relation()
	{
		assertFalse(OsmAreas.isArea(new OsmRelation(0,0, null, null)));

		Map<String, String> tags = new HashMap<>();
		tags.put("type","multipolygon");

		assertTrue(OsmAreas.isArea(new OsmRelation(0, 0, null, tags)));
	}

	@Test public void wayNoTags()
	{
		assertFalse(OsmAreas.isArea(createWay(false, null)));
		assertFalse(OsmAreas.isArea(createWay(true, null)));
	}

	@Test public void waySimple()
	{
		Map<String, String> tags = new HashMap<>();
		tags.put("area","yes");

		assertFalse(OsmAreas.isArea(createWay(false, tags)));
		assertTrue(OsmAreas.isArea(createWay(true, tags)));
	}

	@Test public void wayRuleException()
	{
		Map<String, String> tags = new HashMap<>();
		tags.put("railway","something");

		assertFalse(OsmAreas.isArea(createWay(true, tags)));

		tags.put("railway","station");

		assertTrue(OsmAreas.isArea(createWay(true, tags)));
	}

	@Test public void wayRuleRegex()
	{
		Map<String, String> tags = new HashMap<>();
		tags.put("waterway","duck");

		assertFalse(OsmAreas.isArea(createWay(true, tags)));

		tags.put("waterway","dock");

		assertTrue(OsmAreas.isArea(createWay(true, tags)));
	}

	private Way createWay(boolean ring, Map<String,String> tags)
	{
		List<Long> nodes = ring ? Arrays.asList(0L,1L,0L) : Arrays.asList(0L,1L);
		return new OsmWay(0, 0, nodes, tags );
	}
}
