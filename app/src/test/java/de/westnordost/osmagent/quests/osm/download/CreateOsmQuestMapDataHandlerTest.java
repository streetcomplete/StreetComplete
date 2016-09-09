package de.westnordost.osmagent.quests.osm.download;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

import static org.mockito.Mockito.*;

public class CreateOsmQuestMapDataHandlerTest extends TestCase
{
	private static final LatLon
			P0 = new OsmLatLon(0d,0d),
			P1 = new OsmLatLon(1d,1d),
			P2 = new OsmLatLon(2d,2d);

	private static final Node
			N0 =  new OsmNode(0L,0,P0,null,null),
			N1 =  new OsmNode(1L,0,P1,null,null),
			N2 =  new OsmNode(2L,0,P2,null,null);

	private static final Way
			W0 = new OsmWay(0L,0,Arrays.asList(0L,1L),null,null),
			W1 = new OsmWay(1L,0,Arrays.asList(1L,2L),null,null),
			W2 = new OsmWay(2L,0,Arrays.asList(2L,0L),null,null);

	private static final RelationMember
			RM0 = new OsmRelationMember(0L, "outer", Element.Type.WAY),
			RM1 = new OsmRelationMember(1L, "outer", Element.Type.WAY),
			RM2 = new OsmRelationMember(2L, "inner", Element.Type.WAY);

	private static final Relation
			R0 = new OsmRelation(0L, 0, Arrays.asList(RM0, RM1), null, null),
			R1 = new OsmRelation(0L, 0, Arrays.asList(RM2), null, null);

	public void testCreateNodeQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Node.class);
		ConvenienceTestHandler handler = new ConvenienceTestHandler(questType);
		handler.handle(N0);

		verifyQuestCreation(questType, N0, handler.getQuest(), handler.getElement());

		ElementGeometry geometry = handler.getQuest().getGeometry();
		assertEquals(1, geometry.outer.size());
		assertEquals(1, geometry.outer.get(0).size());
		assertEquals(P0, geometry.outer.get(0).get(0));
		assertNull(geometry.inner);
	}

	public void testCreateWayQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Way.class);
		ConvenienceTestHandler handler = new ConvenienceTestHandler(questType);
		// the nodes that the way references must be added first
		handler.handle(N0, N1, W0);

		verifyQuestCreation(questType, W0, handler.getQuest(), handler.getElement());

		ElementGeometry geometry = handler.getQuest().getGeometry();
		assertEquals(1, geometry.outer.size());
		assertEquals(2, geometry.outer.get(0).size());
		assertEquals(P0, geometry.outer.get(0).get(0));
		assertEquals(P1, geometry.outer.get(0).get(1));
		assertNull(geometry.inner);
	}

	public void testCreateRelationQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Relation.class);
		ConvenienceTestHandler handler = new ConvenienceTestHandler(questType);
		// the ways that the relation references must be added first
		// and the nodes that each of the ways references mus be added even before that
		handler.handle(N0, N1, N2, W0, W1, R0);

		verifyQuestCreation(questType, R0, handler.getQuest(), handler.getElement());

		ElementGeometry geometry = handler.getQuest().getGeometry();
		assertEquals(2, geometry.outer.size());
		assertEquals(0, geometry.inner.size());
		assertEquals(2, geometry.outer.get(0).size());
		assertEquals(P0, geometry.outer.get(0).get(0));
		assertEquals(P1, geometry.outer.get(0).get(1));
		assertEquals(2, geometry.outer.get(1).size());
		assertEquals(P1, geometry.outer.get(1).get(0));
		assertEquals(P2, geometry.outer.get(1).get(1));
	}

	public void testCreateRelationWithInnerPartQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Relation.class);
		ConvenienceTestHandler handler = new ConvenienceTestHandler(questType);
		handler.handle(N0, N2, W2, R1);

		verifyQuestCreation(questType, R1, handler.getQuest(), handler.getElement());

		ElementGeometry geometry = handler.getQuest().getGeometry();
		assertEquals(0, geometry.outer.size());
		assertEquals(1, geometry.inner.size());
		assertEquals(2, geometry.inner.get(0).size());
		assertEquals(P2, geometry.inner.get(0).get(0));
		assertEquals(P0, geometry.inner.get(0).get(1));
	}

	public void testMissingNodes()
	{
		OsmElementQuestType questType = createQuestTypeFor(Way.class);
		Way way = new OsmWay(0, 0, Arrays.asList(0L, 1L), null, null);

		try
		{
			new CreateOsmQuestMapDataHandler(questType, new NullHandler(), null).handle(way);
			fail();
		}
		catch(Exception e) { }
	}

	public void testMissingWays()
	{
		OsmElementQuestType questType = createQuestTypeFor(Relation.class);
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0L, "outer", Element.Type.WAY));
		Relation relation = new OsmRelation(0,0,members,null,null);

		try
		{
			new CreateOsmQuestMapDataHandler(questType, new NullHandler(), null).handle(relation);
			fail();
		}
		catch(Exception e) { }
	}

	public void testMissingNodesInWays()
	{
		OsmElementQuestType questType = createQuestTypeFor(Relation.class);
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0L, "outer", Element.Type.WAY));
		Relation relation = new OsmRelation(0,0,members,null,null);

		CreateOsmQuestMapDataHandler handler = new CreateOsmQuestMapDataHandler(questType,
				new NullHandler(), null);
		handler.handle(new OsmWay(0L,0,Arrays.asList(0L, 1L),null,null));

		try
		{
			handler.handle(relation);
			fail();
		}
		catch(Exception e) { }
	}

	private class NullHandler<T> implements Handler<T> { public void handle(T tea) { } }

	private OsmElementQuestType createQuestTypeFor(Class<? extends Element> elementClass)
	{
		OsmElementQuestType questType = mock(OsmElementQuestType.class);
		when(questType.appliesTo(isA(elementClass))).thenReturn(true);
		return questType;
	}

	private void verifyQuestCreation(OsmElementQuestType questTypeMock, Element element,
									 OsmQuest createdQuest, Element handlerElement)
	{
		assertNotNull(createdQuest);
		verify(questTypeMock).appliesTo(element);
		assertSame(element, handlerElement);
		assertSame(element.getId(), createdQuest.getElementId());
		assertSame(element.getType(), createdQuest.getElementType());
		assertSame(questTypeMock, createdQuest.getType());
	}


	private class ConvenienceTestHandler
	{
		private CreateOsmQuestMapDataHandler handler;
		private SingleElementHandler<OsmQuest> questHandler = new SingleElementHandler<>();
		private SingleElementHandler<Element> elementHandler = new SingleElementHandler<>();

		public ConvenienceTestHandler(OsmElementQuestType questType )
		{
			handler = new CreateOsmQuestMapDataHandler(	questType, questHandler, elementHandler);
		}

		public void handle(Element... elems)
		{
			for(Element elem : elems)
			{
				if(elem instanceof Node)		handler.handle((Node) elem);
				if(elem instanceof Way)			handler.handle((Way) elem);
				if(elem instanceof Relation)	handler.handle((Relation) elem);
			}
		}

		public OsmQuest getQuest()
		{
			return questHandler.get();
		}

		public Element getElement()
		{
			return elementHandler.get();
		}
	}

}
