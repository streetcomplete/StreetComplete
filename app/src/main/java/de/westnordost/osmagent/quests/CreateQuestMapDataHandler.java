package de.westnordost.osmagent.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;

/** Handles incoming map data and creates quests out of it */
public class CreateQuestMapDataHandler implements MapDataHandler
{
	private Handler<Quest> handler;
	private QuestType questType;

	private HashMap<Long, Node> nodes;
	private HashMap<Long, Way> ways;

	public CreateQuestMapDataHandler(QuestType questType, Handler<Quest> handler)
	{
		this.handler = handler;
		this.questType = questType;
		nodes = new HashMap<>();
		ways = new HashMap<>();
	}

	@Override
	public void handle(BoundingBox bounds)
	{
		// ignore
	}

	@Override
	public void handle(Node node)
	{
		nodes.put(node.getId(), node);

		if(questType.appliesTo(node))
		{
			createQuestAndHandle(node, new ElementGeometry(node.getPosition()));
		}
	}

	@Override
	public void handle(Way way)
	{
		ways.put(way.getId(), way);

		if(questType.appliesTo(way))
		{
			createQuestAndHandle(way, new ElementGeometry(getWayGeometry(way)));
		}
	}

	@Override
	public void handle(Relation relation)
	{
		if(questType.appliesTo(relation))
		{
			createQuestAndHandle(relation, getRelationGeometry(relation));
		}
	}

	private void createQuestAndHandle(Element element, ElementGeometry geometry)
	{
		Quest quest = new Quest(questType, element, geometry);
		handler.handle(quest);
	}

	private List<LatLon> getWayGeometry(Way way)
	{
		List<LatLon> geometry = new ArrayList<>(way.getNodeIds().size());
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

	private ElementGeometry getRelationGeometry(Relation relation)
	{
		List<List<LatLon>> outer = new ArrayList<>();
		List<List<LatLon>> inner = new ArrayList<>();
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