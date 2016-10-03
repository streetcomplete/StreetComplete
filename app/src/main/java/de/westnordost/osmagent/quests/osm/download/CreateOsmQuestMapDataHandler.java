package de.westnordost.osmagent.quests.osm.download;

import java.util.HashMap;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;

/** Handles incoming map data and creates quests and element geometry out of it. It passes on the
 *  map data to other handlers. */
public class CreateOsmQuestMapDataHandler implements MapDataHandler, GeometryMapDataProvider
{
	private final Handler<OsmQuest> handler;
	private final Handler<Element> elementHandler;

	private final OsmElementQuestType questType;

	private final HashMap<Long, Node> nodes;
	private final HashMap<Long, Way> ways;

	private final ElementGeometryCreator elementGeometryCreator;

	public CreateOsmQuestMapDataHandler(OsmElementQuestType questType, Handler<OsmQuest> handler,
										Handler<Element> elementHandler)
	{
		this.handler = handler;
		this.elementHandler = elementHandler;
		this.questType = questType;
		this.elementGeometryCreator = new ElementGeometryCreator(this);
		nodes = new HashMap<>();
		ways = new HashMap<>();
	}

	@Override public void handle(BoundingBox bounds)
	{
		// ignore
	}

	@Override public void handle(Node node)
	{
		nodes.put(node.getId(), node);

		if(questType.appliesTo(node))
		{
			createQuestAndHandle(node, elementGeometryCreator.create(node));
		}
	}

	@Override public void handle(Way way)
	{
		ways.put(way.getId(), way);

		if(questType.appliesTo(way))
		{
			createQuestAndHandle(way, elementGeometryCreator.create(way));
		}
	}

	@Override public void handle(Relation relation)
	{
		if(questType.appliesTo(relation))
		{
			createQuestAndHandle(relation, elementGeometryCreator.create(relation));
		}
	}

	private void createQuestAndHandle(Element element, ElementGeometry geometry)
	{
		// invalid geometry -> can't show this quest, so skip it
		if(geometry == null) return;

		elementHandler.handle(element);
		OsmQuest quest = new OsmQuest(questType, element.getType(), element.getId(), geometry);
		handler.handle(quest);
	}

	@Override public Node getNode(long id)
	{
		Node result = nodes.get(id);
		if(result == null)
		{
			throw new RuntimeException("Could not find the node with the id "+id+" in the" +
					" response! Either it has not been requested in the API call, or the" +
					" response is not in the expected format: First nodes, then ways, then" +
					" relations.");
		}
		return result;
	}

	@Override public Way getWay(long id)
	{
		Way result = ways.get(id);
		if(result == null)
		{
			throw new RuntimeException("Could not find the way with the id "+id+" in the" +
					" response! Either it has not been requested in the API call, or the" +
					" response is not in the expected format: First nodes, then ways, then" +
					" relations.");
		}
		return result;
	}
}