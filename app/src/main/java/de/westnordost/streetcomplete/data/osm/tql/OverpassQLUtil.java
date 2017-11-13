package de.westnordost.streetcomplete.data.osm.tql;

import de.westnordost.osmapi.map.data.BoundingBox;

public class OverpassQLUtil
{
	public static String getOverpassBBox(BoundingBox bbox)
	{
		return "[bbox:" +
				bbox.getMinLatitude() + "," + bbox.getMinLongitude() + "," +
				bbox.getMaxLatitude() + "," + bbox.getMaxLongitude() +
				"];";
	}
}
