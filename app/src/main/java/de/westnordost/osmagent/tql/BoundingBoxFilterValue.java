package de.westnordost.osmagent.tql;

import de.westnordost.osmapi.map.data.BoundingBox;

/** A value within a BooleanExpression that filters by a bounding box */
public class BoundingBoxFilterValue implements OQLExpressionValue
{
	private BoundingBox bbox;

	public BoundingBoxFilterValue(BoundingBox bbox)
	{
		this.bbox = bbox;
	}

	@Override
	public boolean matches(Object ele)
	{
		// NOTE: not implemented (yet). Not sure if necessary at all
		return true;
	}

	public String toString()
	{
		// overpass-like / mapnik web-map format
		return "" + bbox.getMinLatitude() + "," + bbox.getMinLongitude() + "," +
				bbox.getMaxLatitude() + "," +  bbox.getMaxLongitude();
	}

	@Override
	public String toOverpassQLString()
	{
		return "(" + toString() + ")";
	}
}
