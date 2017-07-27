package de.westnordost.streetcomplete.data.osm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.util.JTSConst;

/** Information on the geometry of a quest */
public class ElementGeometry implements Serializable
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
		center = findCenterPoint();
	}

	public ElementGeometry(List<List<LatLon>> polylines, List<List<LatLon>> polygons, LatLon center)
	{
		this.polygons = polygons;
		this.polylines = polylines;
		this.center = center;
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof ElementGeometry)) return false;
		ElementGeometry o = (ElementGeometry) other;
		return
				(polylines == null ? o.polylines == null : polylines.equals(o.polylines)) &&
				(polygons == null ? o.polygons == null : polygons.equals(o.polygons));
	}

	private LatLon findCenterPoint()
	{
		try
		{
			Geometry geom = JTSConst.toGeometry(this);
			if(!geom.isValid()) return null;

			if(geom instanceof Polygonal)
			{
				return JTSConst.toLatLon(geom.getInteriorPoint());
			}
			else if(geom instanceof Lineal)
			{
				LengthIndexedLine lil = new LengthIndexedLine(geom);
				return JTSConst.toLatLon(lil.extractPoint(geom.getLength() / 2.0));
			}
		}
		catch (Exception e)
		{
			// unable to create proper geometry...
			return null;
		}
		return null;
	}

	public static class Polygons { public List<List<LatLon>> outer, inner; }
	public Polygons getPolygonsOrderedByOrientation()
	{
		Polygons result = new Polygons();
		result.outer = new ArrayList<>();
		result.inner = new ArrayList<>();
		for(List<LatLon> polygon : polygons)
		{
			boolean isHole = ElementGeometry.isRingDefinedClockwise(polygon);
			if(!isHole)
			{
				result.outer.add(polygon);
			}
			else
			{
				result.inner.add(polygon);
			}
		}
		return result;
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
	public BoundingBox getBounds()
	{
		List<List<LatLon>> points;
		if(polygons != null) points = polygons;
		else if(polylines != null) points = polylines;
		else return new BoundingBox(
				center.getLatitude(), center.getLongitude(),
				center.getLatitude(), center.getLongitude());

		Double latMin = null, lonMin = null, latMax = null, lonMax = null;
		for(List<LatLon> ps : points)
		{
			for(LatLon p : ps)
			{
				double lat = p.getLatitude();
				double lon = p.getLongitude();

				if (latMin == null || latMin > lat) latMin = lat;
				if (latMax == null || latMax < lat) latMax = lat;
				if (lonMin == null || lonMin > lon) lonMin = lon;
				if (lonMax == null || lonMax < lon) lonMax = lon;
			}
		}
		return new BoundingBox(latMin, lonMin, latMax, lonMax);
	}
}
