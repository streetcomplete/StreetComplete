package de.westnordost.streetcomplete.data.osm.download;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.westnordost.streetcomplete.data.meta.OsmAreas;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class ElementGeometryCreator
{
	protected WayGeometrySource data;

	public void setWayGeometryProvider(WayGeometrySource data)
	{
		this.data = data;
	}

	public ElementGeometry create(Node node)
	{
		return new ElementGeometry(node.getPosition());
	}

	public ElementGeometry create(Way way)
	{
		if(data == null) throw new NullPointerException();

		List<LatLon> polyline = data.getNodePositions(way.getId());
		// unable to create geometry
		if(polyline.isEmpty()) return null;
		eliminateDuplicates(polyline);

		List<List<LatLon>> polylines = new ArrayList<>(1);
		polylines.add(polyline);

		ElementGeometry result;
		if(OsmAreas.isArea(way))
		{
			// ElementGeometry considers polygons that are defined clockwise holes, so ensure that
			// it is defined CCW here.
			if(SphericalEarthMath.isRingDefinedClockwise(polyline)) {
				Collections.reverse(polyline);
			}
			result = new ElementGeometry(null, polylines);
		}
		else
		{
			result = new ElementGeometry(polylines, null);
		}
		if(result.center == null) return null;
		return result;
	}

	private void eliminateDuplicates(List<LatLon> polyline)
	{
		Iterator<LatLon> it = polyline.iterator();
		LatLon previous = null;
		while(it.hasNext())
		{
			LatLon line = it.next();
			if(previous == null ||
					line.getLatitude() != previous.getLatitude() ||
					line.getLongitude() != previous.getLongitude())
			{
				previous = line;
			}
			else
			{
				it.remove();
			}
		}
	}

	public ElementGeometry create(Relation relation)
	{
		if(data == null) throw new NullPointerException();

		if(OsmAreas.isArea(relation))
		{
			return createMultipolygonGeometry(relation);
		}
		else
		{
			return createPolylinesGeometry(relation);
		}
	}

	private List<List<LatLon>> getWaysOfRelationWithRole(Relation relation, String role)
	{
		List<List<LatLon>> result = new ArrayList<>();
		for(RelationMember member : relation.getMembers())
		{
			if(member.getType() != Element.Type.WAY) continue;

			long wayId = member.getRef();
			if(role == null || role.equals(member.getRole()))
			{
				List<LatLon> nodePositions = data.getNodePositions(wayId);
				eliminateDuplicates(nodePositions);
				if(nodePositions.size() > 1)
				{
					result.add(nodePositions);
				}
			}
		}
		return result;
	}

	private ElementGeometry createPolylinesGeometry(Relation relation)
	{
		List<List<LatLon>> waysNodePositions = getWaysOfRelationWithRole(relation, null);
		ConnectedWays ways = joinWays(waysNodePositions);

		List<List<LatLon>> polylines = ways.rest;
		polylines.addAll(ways.rings);

		// no valid geometry
		if(polylines.isEmpty()) return null;

		ElementGeometry result = new ElementGeometry(polylines, null);
		if(result.center == null) return null;

		return result;
	}

	private ElementGeometry createMultipolygonGeometry(Relation relation)
	{
		List<List<LatLon>> rings = new ArrayList<>();
		rings.addAll(createNormalizedRingGeometry(relation, "outer", false));
		rings.addAll(createNormalizedRingGeometry(relation, "inner", true));
		// no valid geometry
		if(rings.isEmpty()) return null;

		ElementGeometry result = new ElementGeometry(null, rings);
		if(result.center == null) return null;

		return result;
	}

	private List<List<LatLon>> createNormalizedRingGeometry(Relation relation, String role,
															boolean clockwise)
	{
		List<List<LatLon>> waysNodePositions = getWaysOfRelationWithRole(relation, role);
		List<List<LatLon>> ringGeometry = joinWays(waysNodePositions).rings;
		setOrientation(ringGeometry, clockwise);
		return ringGeometry;
	}

	/** Ensures that all given rings are defined in clockwise/counter-clockwise direction */
	private static void setOrientation(List<List<LatLon>> rings, boolean clockwise)
	{
		for(List<LatLon> ring : rings)
		{
			if(SphericalEarthMath.isRingDefinedClockwise(ring) != clockwise)
			{
				Collections.reverse(ring);
			}
		}
	}

	private static class ConnectedWays
	{
		List<List<LatLon>> rings = new ArrayList<>();
		List<List<LatLon>> rest = new ArrayList<>();
	}

	private static ConnectedWays joinWays(List<List<LatLon>> waysNodePositions)
	{
		NodeWayMap<LatLon> nodeWayMap = new NodeWayMap<>(waysNodePositions);

		ConnectedWays result = new ConnectedWays();

		List<LatLon> currentWay = new ArrayList<>();

		while(nodeWayMap.hasNextNode())
		{
			LatLon node;
			if(currentWay.isEmpty())
			{
				node = nodeWayMap.getNextNode();
			}
			else
			{
				node = currentWay.get(currentWay.size()-1);
			}

			List<List<LatLon>> waysAtNode = nodeWayMap.getWaysAtNode(node);
			if(waysAtNode == null)
			{
				result.rest.add(currentWay);
				currentWay = new ArrayList<>();
			}
			else
			{
				List<LatLon> way = waysAtNode.get(0);

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

	private static boolean isRing(List<?> way)
	{
		return way.get(0).equals(way.get(way.size() - 1));
	}

	/** add <tt>way</tt> to the end of <tt>polyWay</tt>, if necessary in reverse */
	private static void addTo(List<LatLon> way, List<LatLon> polyWay)
	{
		if(polyWay.isEmpty())
		{
			polyWay.addAll(way);
		}
		else
		{
			LatLon addLast = way.get(way.size() - 1);
			LatLon toLast = polyWay.get(polyWay.size() - 1);
			if(addLast.equals(toLast))
			{
				// -1 to not add the last vertex because it has already been added
				ListIterator<LatLon> it = way.listIterator(way.size()-1);
				while(it.hasPrevious())
				{
					polyWay.add(it.previous());
				}
			}
			else
			{
				// +1 to not add the first vertex because it has already been added
				ListIterator<LatLon> it = way.listIterator(+1);
				while(it.hasNext())
				{
					polyWay.add(it.next());
				}
			}
		}
	}

}
