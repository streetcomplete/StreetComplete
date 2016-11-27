package de.westnordost.streetcomplete.data.osm.download;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeWayMapTest extends TestCase
{
	public void testAll()
	{
		List<List<Long>> ways = new ArrayList<>();
		List<Long> way1 = Arrays.asList(1L,2L,3L);
		List<Long> way2 = Arrays.asList(3L,4L,1L);
		List<Long> ring = Arrays.asList(5L,1L,6L,5L);

		ways.add(way1);
		ways.add(way2);
		ways.add(ring);

		NodeWayMap<Long> map = new NodeWayMap<>(ways);

		assertTrue(map.hasNextNode());
		assertEquals(2, map.getWaysAtNode(1L).size());
		assertEquals(2, map.getWaysAtNode(3L).size());
		assertEquals(2, map.getWaysAtNode(5L).size());
		assertNull(map.getWaysAtNode(2L));

		map.removeWay(way1);
		assertEquals(1, map.getWaysAtNode(1L).size());
		assertEquals(1, map.getWaysAtNode(3L).size());

		map.removeWay(way2);
		assertNull(map.getWaysAtNode(1L));
		assertNull(map.getWaysAtNode(3L));

		assertTrue(map.hasNextNode());
		assertEquals(5L, (long) map.getNextNode());

		map.removeWay(ring);

		assertFalse(map.hasNextNode());
	}
}
