package de.westnordost.osmagent.quests.osm;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Information on the geometry of a quest */
public class ElementGeometry
{
	public LatLon center;
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
			center = findCenterPointOfPolygon(polygons);
		}
		else if(polylines != null)
		{
			// if there are more than one polylines, these polylines are not connect to each other,
			// so there is no way to find a reasonable "center point". In most cases however, there
			// is only one polyline, so let's just take the first one...
			center = findCenterPointOfPolyLine(polylines.get(0));
		}
	}

	public ElementGeometry(List<List<LatLon>> polylines, List<List<LatLon>> polygons, LatLon center)
	{
		this.polygons = polygons;
		this.polylines = polylines;
		this.center = center;
	}

	private static LatLon findCenterPointOfPolyLine(List<LatLon> positions)
	{
		int i = 0, j = positions.size() - 1;
		double iLength = 0, jLength = 0;
		double totalILength = 0, totalJLength = 0;

		while(i != j)
		{
			if(totalILength <= totalJLength)
			{
				LatLon a = positions.get(i);
				LatLon b = positions.get(++i);
				iLength = Math.sqrt(
						Math.pow(b.getLongitude() - a.getLongitude(), 2) +
						Math.pow(b.getLatitude() - a.getLatitude(), 2));
				totalILength += iLength;
			}
			else
			{
				LatLon a = positions.get(j);
				LatLon b = positions.get(--j);
				jLength = Math.sqrt(
						Math.pow(b.getLongitude() - a.getLongitude(), 2) +
								Math.pow(b.getLatitude() - a.getLatitude(), 2));
				totalJLength += jLength;
			}
		}

		double centralLineLength;
		if(totalILength == totalJLength)
		{
			return positions.get(i);
		}
		else if(totalILength > totalJLength)
		{
			centralLineLength = iLength;
			totalILength -= iLength;
			--i;
		}
		else
		{
			centralLineLength = jLength;
			totalJLength -= jLength;
			++j;
		}
		// just to be sure that there won't be a div by 0 error ever
		if(centralLineLength == 0)
		{
			return positions.get(i);
		}

		double totalLength = totalILength + totalJLength + centralLineLength;

		double x = (totalLength/2 - totalILength) / centralLineLength;

		LatLon a = positions.get(i);
		LatLon b = positions.get(j);

		double lat = a.getLatitude() + (b.getLatitude() - a.getLatitude()) * x;
		double lon = a.getLongitude() + (b.getLongitude() - a.getLongitude()) * x;

		return new OsmLatLon(lat,lon);
	}

	private static LatLon findCenterPointOfPolygon(List<List<LatLon>> polygons)
	{
		// just find the "average" point... this can be outside of the polygon if it is i.e.
		// banana- or donut shaped. This could be improved with a more elaborate algo later.

		double lat = 0, lon = 0;

		List<LatLon> allPoints = new ArrayList<>();
		for(List<LatLon> polygon : polygons)
		{
			allPoints.addAll(polygon);
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
				(polylines == null ? o.polylines == null : polylines.equals(o.polylines)) &&
				(polygons == null ? o.polygons == null : polygons.equals(o.polygons));
	}
}
