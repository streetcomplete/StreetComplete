package de.westnordost.streetcomplete.data.osm.persist;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Way;

import static org.junit.Assert.assertEquals;


public class WayDaoTest extends ApplicationDbTestCase
{
	private WayDao dao;

	@Before public void createDao()
	{
		dao = new WayDao(dbHelper, serializer);
	}

	@Test public void putGetNoTags()
	{
		Way way = new OsmWay(5, 1, Arrays.asList(1L,2L,3L,4L), null);
		dao.put(way);
		Way dbWay = dao.get(5);

		checkEqual(way, dbWay);
	}

	@Test public void putGetWithTags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("a key", "a value");
		Way way = new OsmWay(5, 1, Arrays.asList(1L,2L,3L,4L), tags);
		dao.put(way);
		Way dbWay = dao.get(5);

		checkEqual(way, dbWay);
	}

	private void checkEqual(Way way, Way dbWay)
	{
		assertEquals(way.getId(), dbWay.getId());
		assertEquals(way.getVersion(), dbWay.getVersion());
		assertEquals(way.getNodeIds(), dbWay.getNodeIds());
		assertEquals(way.getTags(), dbWay.getTags());
	}
}
