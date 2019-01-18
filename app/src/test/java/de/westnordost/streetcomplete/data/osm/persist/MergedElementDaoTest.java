package de.westnordost.streetcomplete.data.osm.persist;

import org.junit.Before;
import org.junit.Test;

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

import static org.mockito.Mockito.*;

public class MergedElementDaoTest
{
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	private MergedElementDao dao;

	@Before public void setUp()
	{
		nodeDao = mock(NodeDao.class);
		wayDao = mock(WayDao.class);
		relationDao = mock(RelationDao.class);
		dao = new MergedElementDao(nodeDao, wayDao, relationDao);
	}

	@Test public void putNode()
	{
		Node node = mock(Node.class);
		when(node.getType()).thenReturn(Element.Type.NODE);
		dao.put(node);
		verify(nodeDao).put(node);
	}

	@Test public void getNode()
	{
		Node node = mock(Node.class);
		when(node.getId()).thenReturn(1L);
		dao.get(Element.Type.NODE, 1L);
		verify(nodeDao).get(1L);
	}

	@Test public void deleteNode()
	{
		dao.delete(Element.Type.NODE, 1L);
		verify(nodeDao).delete(1L);
	}

	@Test public void putWay()
	{
		Way way = mock(Way.class);
		when(way.getType()).thenReturn(Element.Type.WAY);
		dao.put(way);
		verify(wayDao).put(way);
	}

	@Test public void getWay()
	{
		Way way = mock(Way.class);
		when(way.getId()).thenReturn(1L);

		dao.get(Element.Type.WAY, 1L);
		verify(wayDao).get(1L);
	}

	@Test public void deleteWay()
	{
		dao.delete(Element.Type.WAY, 1L);
		verify(wayDao).delete(1L);
	}

	@Test public void putRelation()
	{
		Relation relation = mock(Relation.class);
		when(relation.getType()).thenReturn(Element.Type.RELATION);
		dao.put(relation);
		verify(relationDao).put(relation);
	}

	@Test public void getRelation()
	{
		Relation relation = mock(Relation.class);
		when(relation.getId()).thenReturn(1L);

		dao.get(Element.Type.RELATION, 1L);
		verify(relationDao).get(1L);
	}

	@Test public void deleteRelation()
	{
		dao.delete(Element.Type.RELATION, 1L);
		verify(relationDao).delete(1L);
	}

	@Test public void putAllRelations()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createARelation());

		dao.putAll(elements);
		verify(relationDao).putAll(anyCollection());
	}

	@Test public void putAllWays()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createAWay());

		dao.putAll(elements);
		verify(wayDao).putAll(anyCollection());
	}

	@Test public void putAllNodes()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createANode());

		dao.putAll(elements);
		verify(nodeDao).putAll(anyCollection());
	}

	@Test public void putAllElements()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createANode());
		elements.add(createAWay());
		elements.add(createARelation());

		dao.putAll(elements);
		verify(nodeDao).putAll(anyCollection());
		verify(wayDao).putAll(anyCollection());
		verify(relationDao).putAll(anyCollection());
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

	@Test public void deleteUnreferenced()
	{
		dao.deleteUnreferenced();
		verify(nodeDao).deleteUnreferenced();
		verify(wayDao).deleteUnreferenced();
		verify(relationDao).deleteUnreferenced();
	}
}
