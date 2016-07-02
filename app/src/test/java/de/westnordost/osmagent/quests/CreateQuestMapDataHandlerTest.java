package de.westnordost.osmagent.quests;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.osmagent.quests.types.QuestType;
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

public class CreateQuestMapDataHandlerTest extends TestCase
{
	public void testCreateNodeQuest()
	{
		QuestType questType = createQuestTypeFor(Node.class);

		Node element = new OsmNode(0,0,0,0,null,null);
		SingleElementHandler<Quest> questHandler = new SingleElementHandler<>();
		new CreateQuestMapDataHandler(questType, questHandler).handle(element);

		verifyQuestCreation(questType, element, questHandler.get());
	}

	public void testCreateWayQuest()
	{
		QuestType questType = createQuestTypeFor(Way.class);

		SingleElementHandler<Quest> questHandler = new SingleElementHandler<>();

		CreateQuestMapDataHandler handler = new CreateQuestMapDataHandler(questType, questHandler);
		// the nodes that the way references must be added first
		handler.handle(new OsmNode(0L,0,0,0,null,null));
		handler.handle(new OsmNode(1L,0,0,0,null,null));

		Way element = new OsmWay(0,0,Arrays.asList(0L,1L),null,null);
		handler.handle(element);

		verifyQuestCreation(questType, element, questHandler.get());
	}

	public void testCreateRelationQuest()
	{
		QuestType questType = createQuestTypeFor(Relation.class);

		SingleElementHandler<Quest> questHandler = new SingleElementHandler<>();
		CreateQuestMapDataHandler handler = new CreateQuestMapDataHandler(questType, questHandler);

		// the ways that the relation references must be added first
		// and the nodes that each of the ways references mus be added even before that
		handler.handle(new OsmNode(0L,0,0,0,null,null));
		handler.handle(new OsmNode(1L,0,0,0,null,null));
		handler.handle(new OsmNode(2L,0,0,0,null,null));
		handler.handle(new OsmWay(0L,0,Arrays.asList(0L,1L),null,null));
		handler.handle(new OsmWay(1L,0,Arrays.asList(1L,2L),null,null));
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0L, "outer", Element.Type.WAY));
		members.add(new OsmRelationMember(1L, "outer", Element.Type.WAY));

		Relation element = new OsmRelation(0,0,members,null,null);
		handler.handle(element);

		verifyQuestCreation(questType, element, questHandler.get());
	}

	public void testMissingNodes()
	{
		QuestType questType = createQuestTypeFor(Way.class);
		Way way = new OsmWay(0, 0, Arrays.asList(0L, 1L), null, null);

		try
		{
			new CreateQuestMapDataHandler(questType, new NullHandler()).handle(way);
			fail();
		}
		catch(Exception e) { }
	}

	public void testMissingWays()
	{
		QuestType questType = createQuestTypeFor(Relation.class);
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0L, "outer", Element.Type.WAY));
		Relation relation = new OsmRelation(0,0,members,null,null);

		try
		{
			new CreateQuestMapDataHandler(questType, new NullHandler()).handle(relation);
			fail();
		}
		catch(Exception e) { }
	}

	public void testMissingNodesInWays()
	{
		QuestType questType = createQuestTypeFor(Relation.class);
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0L, "outer", Element.Type.WAY));
		Relation relation = new OsmRelation(0,0,members,null,null);

		CreateQuestMapDataHandler handler = new CreateQuestMapDataHandler(questType, new NullHandler());
		handler.handle(new OsmWay(0L,0,Arrays.asList(0L, 1L),null,null));

		try
		{
			handler.handle(relation);
			fail();
		}
		catch(Exception e) { }
	}

	private class NullHandler implements Handler<Quest> { public void handle(Quest tea) { } }

	private QuestType createQuestTypeFor(Class<? extends Element> elementClass)
	{
		QuestType questType = mock(QuestType.class);
		when(questType.appliesTo(isA(elementClass))).thenReturn(true);
		return questType;
	}

	private void verifyQuestCreation(QuestType questTypeMock, Element element, Quest createdQuest)
	{
		assertNotNull(createdQuest);
		verify(questTypeMock).appliesTo(element);
		assertSame(element, createdQuest.getElement());
		assertSame(questTypeMock, createdQuest.getType());
	}
}
