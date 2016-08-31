package de.westnordost.osmagent.quests.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Information on the geometry of a quest */
public class ElementGeometry implements Serializable
{
	public LatLon center;

	public List<List<LatLon>> outer;
	public List<List<LatLon>> inner;

	public ElementGeometry(LatLon position)
	{
		this(Collections.singletonList(Collections.singletonList(position)), null, position);
	}

	public ElementGeometry(List<LatLon> positions)
	{
		this(Collections.singletonList(positions), null, findCenterPointOfPolyLine(positions));
	}

	public ElementGeometry(List<List<LatLon>> outer, List<List<LatLon>> inner)
	{
		this(outer, inner, findCenterPointOfPolygon(outer, inner));
	}

	public ElementGeometry(List<List<LatLon>> outer, List<List<LatLon>> inner, LatLon center)
	{
		this.outer = outer;
		this.inner = inner;
		this.center = center;
	}

	private static LatLon findCenterPointOfPolyLine(List<LatLon> positions)
	{
		if(positions.size() % 2 == 1)
		{
			return positions.get(positions.size()/2);
		}
		else
		{
			LatLon pos1 = positions.get(positions.size()/2-1);
			LatLon pos2 = positions.get(positions.size()/2);
			return new OsmLatLon(
					(pos1.getLatitude() + pos2.getLatitude()) / 2d,
					(pos1.getLongitude() + pos2.getLongitude()) / 2d);
		}
	}

	private static LatLon findCenterPointOfPolygon(List<List<LatLon>> outer, List<List<LatLon>> inner)
	{
		// just find the "average" point... this can be outside of the polygon if it is i.e.
		// banana- or donut shaped. This could be improved with a more elaborate algo later.

		double lat = 0, lon = 0;

		ArrayList<LatLon> allPoints = new ArrayList<>();
		for(List<LatLon> outerList : outer)
		{
			allPoints.addAll(outerList);
		}
		for(List<LatLon> innerList : inner)
		{
			allPoints.addAll(innerList);
		}

		double pointCount = allPoints.size();

		for(LatLon point : allPoints)
		{
			lat += point.getLatitude() / pointCount;
			lon += point.getLongitude() / pointCount;
		}

		return new OsmLatLon(lat, lon);
	}


	// TODO add tests in CreateQuestMapDataHandlerTest

	// TODO add marker location, bbox(?) - at least it should be available in a OsmQuest and queryable in DB
}
