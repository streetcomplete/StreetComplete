package de.westnordost.osmagent.quests.osm.download;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.SingleElementHandler;
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

public class CreateOsmQuestMapDataHandlerTest extends TestCase
{
	public void testCreateNodeQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Node.class);

		Node element = new OsmNode(0,0,0d,0d,null,null);
		SingleElementHandler<OsmQuest> questHandler = new SingleElementHandler<>();
		SingleElementHandler<Node> nodeHandler = new SingleElementHandler<>();

		new CreateOsmQuestMapDataHandler(questType, questHandler, nodeHandler, null, null).handle(element);

		verifyQuestCreation(questType, element, questHandler.get(), nodeHandler.get());
	}

	public void testCreateWayQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Way.class);

		SingleElementHandler<OsmQuest> questHandler = new SingleElementHandler<>();
		SingleElementHandler<Way> wayHandler = new SingleElementHandler<>();

		CreateOsmQuestMapDataHandler handler = new CreateOsmQuestMapDataHandler(
				questType, questHandler, null, wayHandler, null);
		// the nodes that the way references must be added first
		handler.handle(new OsmNode(0L,0,0d,0d,null,null));
		handler.handle(new OsmNode(1L,0,0d,0d,null,null));

		Way element = new OsmWay(0,0,Arrays.asList(0L,1L),null,null);
		handler.handle(element);

		verifyQuestCreation(questType, element, questHandler.get(), wayHandler.get());
	}

	public void testCreateRelationQuest()
	{
		OsmElementQuestType questType = createQuestTypeFor(Relation.class);

		SingleElementHandler<OsmQuest> questHandler = new SingleElementHandler<>();
		SingleElementHandler<Relation> relationHandler = new SingleElementHandler<>();

		CreateOsmQuestMapDataHandler handler = new CreateOsmQuestMapDataHandler(
				questType, questHandler, null, null, relationHandler);

		// the ways that the relation references must be added first
		// and the nodes that each of the ways references mus be added even before that
		handler.handle(new OsmNode(0L,0,0d,0d,null,null));
		handler.handle(new OsmNode(1L,0,0d,0d,null,null));
		handler.handle(new OsmNode(2L,0,0d,0d,null,null));
		handler.handle(new OsmWay(0L,0,Arrays.asList(0L,1L),null,null));
		handler.handle(new OsmWay(1L,0,Arrays.asList(1L,2L),null,null));
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0L, "outer", Element.Type.WAY));
		members.add(new OsmRelationMember(1L, "outer", Element.Type.WAY));

		Relation element = new OsmRelation(0,0,members,null,null);
		handler.handle(element);

		verifyQuestCreation(questType, element, questHandler.get(), relationHandler.get());
	}

	public void testMissingNodes()
	{
		OsmElementQuestType questType = createQuestTypeFor(Way.class);
		Way way = new OsmWay(0, 0, Arrays.asList(0L, 1L), null, null);

		try
		{
			new CreateOsmQuestMapDataHandler(questType, new NullHandler(), null, null, null).handle(way);
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
			new CreateOsmQuestMapDataHandler(questType, new NullHandler(),
					null, null, null).handle(relation);
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
				new NullHandler(), null, null, null);
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
}
