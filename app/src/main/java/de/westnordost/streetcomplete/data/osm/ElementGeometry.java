package de.westnordost.streetcomplete.data.osm;

import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

/** Information on the geometry of a quest */
public class ElementGeometry
{
	public LatLon center;
	//* polygons are considered holes if they are defined clockwise */
	public List<List<LatLon>> polygons = null;
	public List<List<LatLon>> polylines = null;

	public ElementGeometry(LatLon center)
	{
		this.center = center;
	}

	public ElementGeometry(List<List<LatLon>> polylines, List<List<LatLon>> polygons)
	{
		this.polygons = polygons;
		this.polylines = polylines;
		if(polygons != null)
		{
			center = findCenterPointOfMultiPolygon(polygons);
		}
		else if(polylines != null)
		{
			center = findCenterPointOfPolyLines(polylines);
		}
	}

	public ElementGeometry(List<List<LatLon>> polylines, List<List<LatLon>> polygons, LatLon center)
	{
		this.polygons = polygons;
		this.polylines = polylines;
		this.center = center;
	}

	private static LatLon findCenterPointOfPolyLines(List<List<LatLon>> polylines)
	{
		// if there are more than one polylines, these polylines are not connect to each other,
		// so there is no way to find a reasonable "center point". In most cases however, there
		// is only one polyline, so let's just take the first one...
		// This is the same behavior as Leaflet or Tangram
		return findCenterPointOfPolyLine(polylines.get(0));
	}

	private static LatLon findCenterPointOfPolyLine(List<LatLon> positions)
	{
		double halfDistance = getLengthInMeters(positions) / 2;

		if(halfDistance == 0) return positions.get(0);

		double distance = 0;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon pos1 = positions.get(i);
			LatLon pos2 = positions.get(i+1);
			double segmentDistance = SphericalEarthMath.distance(pos1, pos2);
			distance += segmentDistance;

			if(distance > halfDistance)
			{
				double ratio = (distance - halfDistance) / segmentDistance;
				double lat = pos2.getLatitude() - ratio * (pos2.getLatitude() - pos1.getLatitude());
				double lon = pos2.getLongitude() - ratio * (pos2.getLongitude() - pos1.getLongitude());
				return new OsmLatLon(lat, lon);
			}
		}
		return null;
	}

	private static LatLon findCenterPointOfMultiPolygon(List<List<LatLon>> polygons)
	{
		// only use first ring that is not a hole if there are multiple (clockwise == holes)
		// this is the same behavior as Leaflet or Tangram
		for(List<LatLon> polygon : polygons)
		{
			if(!isRingDefinedClockwise(polygon))
				return findCenterPointOfPolygon(polygon);
		}
		return null;
	}

	private static LatLon findCenterPointOfPolygon(List<LatLon> polygon) {

		double lon = 0, lat = 0, area = 0;
		int len = polygon.size();

		for(int i = 0, j = len-1; i<len; j = i, ++i)
		{
			LatLon pos1 = polygon.get(i);
			LatLon pos2 = polygon.get(j);

			double f = pos1.getLongitude() * pos2.getLatitude() - pos2.getLongitude() * pos1.getLatitude();
			lon += (pos1.getLongitude() + pos2.getLongitude()) * f;
			lat += (pos1.getLatitude() + pos2.getLatitude()) * f;
			area += f * 3;
		}

		if(area == 0) {
			return null;
		}
		return new OsmLatLon(lat / area, lon / area);
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof ElementGeometry)) return false;
		ElementGeometry o = (ElementGeometry) other;
		return
				(polylines == null ? o.polylines == null : polylines.equals(o.polylines)) &&
				(polygons == null ? o.polygons == null : polygons.equals(o.polygons));
	}

	public static boolean isRingDefinedClockwise(List<LatLon> ring)
	{
		double sum = 0;
		int len = ring.size();
		for(int i = 0, j = len-1; i<len; j = i, ++i)
		{
			LatLon pos1 = ring.get(j);
			LatLon pos2 = ring.get(i);
			sum += pos1.getLongitude() * pos2.getLatitude() - pos2.getLongitude() * pos1.getLatitude();
		}
		return sum > 0;
	}

	private static double getLengthInMeters(List<LatLon> positions)
	{
		double length = 0;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon p0 = positions.get(i);
			LatLon p1 = positions.get(i+1);
			length += SphericalEarthMath.distance(p0, p1);
		}
		return length;
	}
}
