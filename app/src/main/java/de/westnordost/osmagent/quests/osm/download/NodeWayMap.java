package de.westnordost.osmagent.quests.osm.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Knows which vertices connect which ways */
public class NodeWayMap
{
	private final Map<Long, List<List<Long>>> wayEndpoints = new TreeMap<>();

	public NodeWayMap(List<List<Long>> ways)
	{
		for (List<Long> way : ways)
		{
			Long firstNodeId = way.get(0);
			Long lastNodeId = way.get(way.size() - 1);

			if (!wayEndpoints.containsKey(firstNodeId))
			{
				wayEndpoints.put(firstNodeId, new ArrayList<List<Long>>());
			}
			if (!wayEndpoints.containsKey(lastNodeId))
			{
				wayEndpoints.put(lastNodeId, new ArrayList<List<Long>>());
			}
			wayEndpoints.get(firstNodeId).add(way);
			wayEndpoints.get(lastNodeId).add(way);
		}
	}

	public boolean hasNextNodeId()
	{
		return !wayEndpoints.isEmpty();
	}

	public long getNextNodeId()
	{
		return wayEndpoints.keySet().iterator().next();
	}

	public List<List<Long>> getWaysAtNode(Long nodeId)
	{
		return wayEndpoints.get(nodeId);
	}

	public void removeWay(List<Long> way)
	{
		Iterator<List<List<Long>>> it = wayEndpoints.values().iterator();
		while(it.hasNext())
		{
			List<List<Long>> waysPerNode = it.next();

			Iterator<List<Long>> waysIt = waysPerNode.iterator();
			while(waysIt.hasNext())
			{
				if(waysIt.next() == way)
				{
					waysIt.remove();
				}
			}

			if(waysPerNode.isEmpty())
			{
				it.remove();
			}
		}
	}
}
