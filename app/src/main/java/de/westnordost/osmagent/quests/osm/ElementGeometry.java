package de.westnordost.osmagent.quests.osm;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Information on the geometry of a quest */
public class ElementGeometry
{
	public LatLon center;
	public ArrayList<ArrayList<LatLon>> outer;
	public ArrayList<ArrayList<LatLon>> inner;

	public ElementGeometry()
	{

	}

	public ElementGeometry(LatLon position)
	{
		this(createSingleElementList(createSingleElementList(position)), null, position);
	}

	public ElementGeometry(ArrayList<LatLon> positions)
	{
		this(createSingleElementList(positions), null, findCenterPointOfPolyLine(positions));
	}

	public ElementGeometry(ArrayList<ArrayList<LatLon>> outer, ArrayList<ArrayList<LatLon>> inner)
	{
		this(outer, inner, findCenterPointOfPolygon(outer, inner));
	}

	public ElementGeometry(ArrayList<ArrayList<LatLon>> outer, ArrayList<ArrayList<LatLon>> inner, LatLon center)
	{
		this.outer = outer;
		this.inner = inner;
		this.center = center;
	}

	private static <T> ArrayList<T> createSingleElementList(T position)
	{
		// not using Collections.singletonList because Kryo seems to have a problem with that
		ArrayList<T> list = new ArrayList<>(1);
		list.add(position);
		return list;
	}

	private static LatLon findCenterPointOfPolyLine(ArrayList<LatLon> positions)
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

	private static LatLon findCenterPointOfPolygon(ArrayList<ArrayList<LatLon>> outer,
												   ArrayList<ArrayList<LatLon>> inner)
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

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof ElementGeometry)) return false;
		ElementGeometry o = (ElementGeometry) other;
		return
				o.center.equals(center) &&
				(outer == null ? o.outer == null : outer.equals(o.outer)) &&
				(inner == null ? o.inner == null : inner.equals(o.inner));
	}

	// TODO add tests in CreateQuestMapDataHandlerTest

	// TODO add marker location, bbox(?) - at least it should be available in a OsmQuest and queryable in DB
}
