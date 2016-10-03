package de.westnordost.osmagent.quests.osm.download;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import de.westnordost.osmagent.meta.OsmAreas;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

public class ElementGeometryCreator
{
	private GeometryMapDataProvider data;

	public ElementGeometryCreator(GeometryMapDataProvider data)
	{
		this.data = data;
	}

	public ElementGeometry create(Node node)
	{
		return new ElementGeometry(node.getPosition());
	}

	public ElementGeometry create(Way way)
	{
		List<LatLon> polyline = createWayGeometry(way.getNodeIds());
		// unable to create geometry
		if(polyline.isEmpty()) return null;

		List<List<LatLon>> polylines = new ArrayList<>(1);
		polylines.add(polyline);

		if(OsmAreas.isArea(way))
		{
			return new ElementGeometry(null, polylines);
		}
		else
		{
			return new ElementGeometry(polylines, null);
		}
	}

	public ElementGeometry create(Relation relation)
	{
		if(OsmAreas.isArea(relation))
		{
			return createMultipolygonGeometry(relation);
		}
		else
		{
			return createPolylinesGeometry(relation);
		}
	}

	private List<LatLon> createWayGeometry(List<Long> wayNodeIds)
	{
		List<LatLon> geometry = new ArrayList<>(wayNodeIds.size());
		for(long nodeId : wayNodeIds)
		{
			geometry.add(data.getNode(nodeId).getPosition());
		}
		return geometry;
	}

	private List<List<Long>> getWaysOfRelationWithRole(Relation relation, String role)
	{
		List<List<Long>> result = new ArrayList<>();
		for(RelationMember member : relation.getMembers())
		{
			if(member.getType() != Element.Type.WAY) continue;

			long wayId = member.getRef();
			if(role == null || role.equals(member.getRole()))
			{
				List<Long> nodeIds = data.getWay(wayId).getNodeIds();
				if(nodeIds.size() > 1)
				{
					result.add(nodeIds);
				}
			}
		}
		return result;
	}

	private ElementGeometry createPolylinesGeometry(Relation relation)
	{
		List<List<Long>> waysNodeIds = getWaysOfRelationWithRole(relation, null);
		ConnectedWays ways = joinWays(waysNodeIds);

		List<List<Long>> joinedWaysNodeIds = ways.rest;
		joinedWaysNodeIds.addAll(ways.rings);

		List<List<LatLon>> polylines = createWaysGeometry(joinedWaysNodeIds);

		// no valid geometry
		if(polylines.isEmpty()) return null;

		return new ElementGeometry(polylines, null);
	}

	private ElementGeometry createMultipolygonGeometry(Relation relation)
	{
		List<List<LatLon>> rings = new ArrayList<>();
		rings.addAll(createNormalizedRingGeometry(relation, "outer", false));
		rings.addAll(createNormalizedRingGeometry(relation, "inner", true));
		// no valid geometry
		if(rings.isEmpty()) return null;

		return new ElementGeometry(null, rings);
	}

	private List<List<LatLon>> createNormalizedRingGeometry(Relation relation, String role,
															boolean clockwise)
	{
		List<List<Long>> waysNodeIds = getWaysOfRelationWithRole(relation, role);
		List<List<Long>> ringsNodeIds = joinWays(waysNodeIds).rings;
		List<List<LatLon>> ringGeometry = createWaysGeometry(ringsNodeIds);
		setOrientation(ringGeometry, clockwise);
		return ringGeometry;
	}

	private List<List<LatLon>> createWaysGeometry(List<List<Long>> waysNodeIds)
	{
		List<List<LatLon>> result = new ArrayList<>(waysNodeIds.size());
		for(List<Long> wayNodeIds : waysNodeIds)
		{
			result.add(createWayGeometry(wayNodeIds));
		}
		return result;
	}

	/** Ensures that all given rings are defined in clockwise/counter-clockwise direction */
	private static void setOrientation(List<List<LatLon>> rings, boolean clockwise)
	{
		for(List<LatLon> ring : rings)
		{
			if(isRingDefinedClockwise(ring) != clockwise)
			{
				Collections.reverse(ring);
			}
		}
	}

	private static class ConnectedWays
	{
		List<List<Long>> rings = new ArrayList<>();
		List<List<Long>> rest = new ArrayList<>();
	}

	private static ConnectedWays joinWays(List<List<Long>> waysNodeIds)
	{
		NodeWayMap nodeWayMap = new NodeWayMap(waysNodeIds);

		ConnectedWays result = new ConnectedWays();

		List<Long> currentWay = new ArrayList<>();

		while(nodeWayMap.hasNextNodeId())
		{
			Long nodeId;
			if(currentWay.isEmpty())
			{
				nodeId = nodeWayMap.getNextNodeId();
			}
			else
			{
				nodeId = currentWay.get(currentWay.size()-1);
			}

			List<List<Long>> waysAtNode = nodeWayMap.getWaysAtNode(nodeId);
			if(waysAtNode == null)
			{
				result.rest.add(currentWay);
				currentWay = new ArrayList<>();
			}
			else
			{
				List<Long> way = waysAtNode.get(0);

				addTo(way, currentWay);
				nodeWayMap.removeWay(way);

				// finish ring and start new one
				if(isRing(currentWay))
				{
					result.rings.add(currentWay);
					currentWay = new ArrayList<>();
				}
			}
		}
		if(!currentWay.isEmpty())
		{
			result.rest.add(currentWay);
		}

		return result;
	}

	private static boolean isRing(List<Long> way)
	{
		return way.get(0).equals(way.get(way.size() - 1));
	}

	/** add <tt>way</tt> to the end of <tt>polyWay</tt>, if necessary in reverse */
	private static void addTo(List<Long> way, List<Long> polyWay)
	{
		if(polyWay.isEmpty())
		{
			polyWay.addAll(way);
		}
		else
		{
			long addLast = way.get(way.size() - 1);
			long toLast = polyWay.get(polyWay.size() - 1);
			if(addLast == toLast)
			{
				way = Lists.reverse(way);
			}
			// +1 to not add the first vertex because it has already been added
			ListIterator<Long> it = way.listIterator(1);
			while(it.hasNext())
			{
				polyWay.add(it.next());
			}
		}
	}

	private static boolean isRingDefinedClockwise(List<LatLon> ring)
	{
		double sum = 0;
		for(int i=0; i<ring.size(); ++i)
		{
			LatLon pos1 = ring.get(i);
			LatLon pos2 = ring.get((i+1) % ring.size());
			sum += (pos2.getLongitude() - pos1.getLongitude()) * (pos2.getLatitude() + pos1.getLatitude());
		}
		return sum > 0;
	}
}
