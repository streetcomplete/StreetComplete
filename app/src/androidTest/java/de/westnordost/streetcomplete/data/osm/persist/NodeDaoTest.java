package de.westnordost.streetcomplete.data.osm.persist;

import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;


public class NodeDaoTest extends ApplicationDbTestCase
{
	private NodeDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dao = new NodeDao(dbHelper, serializer);
	}

	public void testPutGetNoTags()
	{
		LatLon pos = new OsmLatLon(2,2);
		Node node = new OsmNode(5, 1, pos, null);
		dao.put(node);
		Node dbNode = dao.get(5);

		checkEqual(node, dbNode);
	}

	public void testPutGetWithTags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("a key", "a value");
		LatLon pos = new OsmLatLon(2,2);
		Node node = new OsmNode(5, 1, pos, tags);
		dao.put(node);
		Node dbNode = dao.get(5);

		checkEqual(node, dbNode);
	}

	private void checkEqual(Node node, Node dbNode)
	{
		assertEquals(node.getId(), dbNode.getId());
		assertEquals(node.getVersion(), dbNode.getVersion());
		assertEquals(node.getPosition(), dbNode.getPosition());
		assertEquals(node.getTags(), dbNode.getTags());
	}
}
