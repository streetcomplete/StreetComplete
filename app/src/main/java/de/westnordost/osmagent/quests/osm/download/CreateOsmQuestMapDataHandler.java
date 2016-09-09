package de.westnordost.osmagent.quests.osm.download;

import java.util.ArrayList;
import java.util.HashMap;

import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;

/** Handles incoming map data and creates quests and element geometry out of it. It passes on the
 *  map data to other handlers. */
public class CreateOsmQuestMapDataHandler implements MapDataHandler
{
	private final Handler<OsmQuest> handler;
	private final Handler<Element> elementHandler;

	private final OsmElementQuestType questType;

	private final HashMap<Long, Node> nodes;
	private final HashMap<Long, Way> ways;

	public CreateOsmQuestMapDataHandler(OsmElementQuestType questType, Handler<OsmQuest> handler,
										Handler<Element> elementHandler)
	{
		this.handler = handler;
		this.elementHandler = elementHandler;
		this.questType = questType;
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
			ElementGeometry geometry = new ElementGeometry(node.getPosition());
			createQuestAndHandle(node, geometry);
		}
	}

	@Override public void handle(Way way)
	{
		ways.put(way.getId(), way);

		if(questType.appliesTo(way))
		{
			ElementGeometry geometry = new ElementGeometry(getWayGeometry(way));
			createQuestAndHandle(way, geometry);
		}
	}

	@Override public void handle(Relation relation)
	{
		if(questType.appliesTo(relation))
		{
			ElementGeometry geometry = createRelationGeometry(relation);
			createQuestAndHandle(relation, geometry);
		}
	}

	private void createQuestAndHandle(Element element, ElementGeometry geometry)
	{
		elementHandler.handle(element);
		OsmQuest quest = new OsmQuest(questType, element.getType(), element.getId(), geometry);
		handler.handle(quest);
	}

	private ArrayList<LatLon> getWayGeometry(Way way)
	{
		ArrayList<LatLon> geometry = new ArrayList<>(way.getNodeIds().size());
		for(long nodeId : way.getNodeIds())
		{
			if(!nodes.containsKey(nodeId))
			{
				throw new RuntimeException("Could not find the node with the id "+nodeId+" in the" +
						" response! Either it has not been requested in the API call, or the" +
						" response is not in the expected format: First nodes, then ways, then" +
						" relations.");
			}

			geometry.add(nodes.get(nodeId).getPosition());
		}
		return geometry;
	}

	private ElementGeometry createRelationGeometry(Relation relation)
	{
		ArrayList<ArrayList<LatLon>> outer = new ArrayList<>();
		ArrayList<ArrayList<LatLon>> inner = new ArrayList<>();
		for(RelationMember member : relation.getMembers())
		{
			if(member.getType() == Element.Type.WAY)
			{
				long wayId = member.getRef();
				if(!ways.containsKey(wayId))
				{
					throw new RuntimeException("Could not find the way with the id "+wayId+" in the" +
							" response! Either it has not been requested in the API call, or the" +
							" response is not in the expected format: First nodes, then ways, then" +
							" relations.");
				}

				if("outer".equals(member.getRole()))
				{
					outer.add(getWayGeometry(ways.get(wayId)));
				}
				else if("inner".equals(member.getRole()))
				{
					inner.add(getWayGeometry(ways.get(wayId)));
				}
			}
		}
		return new ElementGeometry(outer, inner);
	}

}