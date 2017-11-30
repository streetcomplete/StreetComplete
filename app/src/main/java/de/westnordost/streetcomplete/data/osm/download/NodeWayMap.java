package de.westnordost.streetcomplete.data.osm.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Knows which vertices connect which ways. T is the identifier of a vertex */
public class NodeWayMap<T>
{
	private final Map<T, List<List<T>>> wayEndpoints = new HashMap<>();

	public NodeWayMap(List<List<T>> ways)
	{
		for (List<T> way : ways)
		{
			T firstNode = way.get(0);
			T lastNode = way.get(way.size() - 1);

			if (!wayEndpoints.containsKey(firstNode))
			{
				wayEndpoints.put(firstNode, new ArrayList<>());
			}
			if (!wayEndpoints.containsKey(lastNode))
			{
				wayEndpoints.put(lastNode, new ArrayList<>());
			}
			wayEndpoints.get(firstNode).add(way);
			wayEndpoints.get(lastNode).add(way);
		}
	}

	public boolean hasNextNode()
	{
		return !wayEndpoints.isEmpty();
	}

	public T getNextNode()
	{
		return wayEndpoints.keySet().iterator().next();
	}

	public List<List<T>> getWaysAtNode(T node)
	{
		return wayEndpoints.get(node);
	}

	public void removeWay(List<T> way)
	{
		Iterator<List<List<T>>> it = wayEndpoints.values().iterator();
		while(it.hasNext())
		{
			List<List<T>> waysPerNode = it.next();

			Iterator<List<T>> waysIt = waysPerNode.iterator();
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
