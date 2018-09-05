package de.westnordost.streetcomplete.data.osm;

import java.io.Serializable;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.util.FlattenIterable;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

/** Information on the geometry of a quest */
public class ElementGeometry implements Serializable
{
	public final LatLon center;
	//* polygons are considered holes if they are defined clockwise */
	public final List<List<LatLon>> polygons;
	public final List<List<LatLon>> polylines;

	private transient BoundingBox bbox = null;

	public ElementGeometry(LatLon center)
	{
		this.center = center;
		polylines = null;
		polygons = null;
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
		else throw new IllegalArgumentException("Either polylines or polygons must not be null");
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

	@Override public int hashCode()
	{
		int result = polygons != null ? polygons.hashCode() : 0;
		result = 31 * result + (polylines != null ? polylines.hashCode() : 0);
		return result;
	}

	public BoundingBox getBounds()
	{
		if(bbox == null)
		{
			List<List<LatLon>> points;
			if (polygons != null) points = polygons;
			else if (polylines != null) points = polylines;
			else return new BoundingBox(
					center.getLatitude(), center.getLongitude(),
					center.getLatitude(), center.getLongitude());
			FlattenIterable<LatLon> itb = new FlattenIterable<>(LatLon.class);
			itb.add(points);
			bbox = SphericalEarthMath.enclosingBoundingBox(itb);
		}
		return bbox;
	}

	private static LatLon findCenterPointOfPolyLines(List<List<LatLon>> polylines)
	{
		// if there are more than one polylines, these polylines are not connect to each other,
		// so there is no way to find a reasonable "center point". In most cases however, there
		// is only one polyline, so let's just take the first one...
		// This is the same behavior as Leaflet or Tangram
		return SphericalEarthMath.centerPointOfPolyline(polylines.get(0));
	}

	private static LatLon findCenterPointOfMultiPolygon(List<List<LatLon>> polygons)
	{
		// only use first ring that is not a hole if there are multiple (clockwise == holes)
		// this is the same behavior as Leaflet or Tangram
		for(List<LatLon> polygon : polygons)
		{
			if(!SphericalEarthMath.isRingDefinedClockwise(polygon))
				return SphericalEarthMath.centerPointOfPolygon(polygon);
		}
		return null;
	}
}
