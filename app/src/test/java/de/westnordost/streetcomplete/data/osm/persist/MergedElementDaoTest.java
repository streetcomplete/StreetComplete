package de.westnordost.streetcomplete.data.osm.persist;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MergedElementDaoTest extends TestCase
{
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	private MergedElementDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		nodeDao = mock(NodeDao.class);
		wayDao = mock(WayDao.class);
		relationDao = mock(RelationDao.class);
		dao = new MergedElementDao(nodeDao, wayDao, relationDao);
	}

	public void testPutNode()
	{
		Node node = mock(Node.class);
		when(node.getType()).thenReturn(Element.Type.NODE);
		dao.put(node);
		verify(nodeDao).put(node);
	}

	public void testGetNode()
	{
		Node node = mock(Node.class);
		when(node.getId()).thenReturn(1L);
		dao.get(Element.Type.NODE, 1L);
		verify(nodeDao).get(1L);
	}

	public void testDeleteNode()
	{
		dao.delete(Element.Type.NODE, 1L);
		verify(nodeDao).delete(1L);
	}

	public void testPutWay()
	{
		Way way = mock(Way.class);
		when(way.getType()).thenReturn(Element.Type.WAY);
		dao.put(way);
		verify(wayDao).put(way);
	}

	public void testGetWay()
	{
		Way way = mock(Way.class);
		when(way.getId()).thenReturn(1L);

		dao.get(Element.Type.WAY, 1L);
		verify(wayDao).get(1L);
	}

	public void testDeleteWay()
	{
		dao.delete(Element.Type.WAY, 1L);
		verify(wayDao).delete(1L);
	}

	public void testPutRelation()
	{
		Relation relation = mock(Relation.class);
		when(relation.getType()).thenReturn(Element.Type.RELATION);
		dao.put(relation);
		verify(relationDao).put(relation);
	}

	public void testGetRelation()
	{
		Relation relation = mock(Relation.class);
		when(relation.getId()).thenReturn(1L);

		dao.get(Element.Type.RELATION, 1L);
		verify(relationDao).get(1L);
	}

	public void testDeleteRelation()
	{
		dao.delete(Element.Type.RELATION, 1L);
		verify(relationDao).delete(1L);
	}

	public void testPutAllRelations()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createARelation());

		dao.putAll(elements);
		verify(relationDao).putAll(anyCollectionOf(Relation.class));
	}

	public void testPutAllWays()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createAWay());

		dao.putAll(elements);
		verify(wayDao).putAll(anyCollectionOf(Way.class));
	}

	public void testPutAllNodes()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createANode());

		dao.putAll(elements);
		verify(nodeDao).putAll(anyCollectionOf(Node.class));
	}

	public void testPutAllElements()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createANode());
		elements.add(createAWay());
		elements.add(createARelation());

		dao.putAll(elements);
		verify(nodeDao).putAll(anyCollectionOf(Node.class));
		verify(wayDao).putAll(anyCollectionOf(Way.class));
		verify(relationDao).putAll(anyCollectionOf(Relation.class));
	}

	private Node createANode()
	{
		return new OsmNode(0,0, 0.0, 0.0, null);
	}

	private Way createAWay()
	{
		return new OsmWay(0,0, Collections.singletonList(0L), null);
	}

	private Relation createARelation()
	{
		RelationMember m = new OsmRelationMember(0, "", Element.Type.NODE);
		return new OsmRelation(0,0, Collections.singletonList(m), null);
	}

	public void testDeleteUnreferenced()
	{
		dao.deleteUnreferenced();
		verify(nodeDao).deleteUnreferenced();
		verify(wayDao).deleteUnreferenced();
		verify(relationDao).deleteUnreferenced();
	}
}
