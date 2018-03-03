package de.westnordost.streetcomplete.data.osm.tql;

import de.westnordost.osmapi.map.data.BoundingBox;

public class OverpassQLUtil
{
	public static String getGlobalOverpassBBox(BoundingBox bbox)
	{
		return "[bbox:" + getBboxString(bbox) + "];";
	}

	public static String getOverpassBboxFilter(BoundingBox bbox)
	{
		return "(" + getBboxString(bbox) + ")";
	}

	private static String getBboxString(BoundingBox bbox)
	{
		return bbox.getMinLatitude() + "," + bbox.getMinLongitude() + "," +
			bbox.getMaxLatitude() + "," + bbox.getMaxLongitude();
	}
}
