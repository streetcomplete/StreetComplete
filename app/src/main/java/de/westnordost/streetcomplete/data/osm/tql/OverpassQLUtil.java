package de.westnordost.streetcomplete.data.osm.tql;

import java.text.NumberFormat;
import java.util.Locale;

import de.westnordost.osmapi.map.data.BoundingBox;

public class OverpassQLUtil
{
	public static final int DEFAULT_MAX_QUESTS = 2000;

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
		NumberFormat df = NumberFormat.getNumberInstance(Locale.US);
		df.setMaximumFractionDigits(340);

		return df.format(bbox.getMinLatitude()) + "," + df.format(bbox.getMinLongitude()) + "," +
			df.format(bbox.getMaxLatitude()) + "," + df.format(bbox.getMaxLongitude());
	}

	public static String getQuestPrintStatement()
	{
		// by default we limit the number of quests created to something that does not cause
		// performance problems
		return "out meta geom "+DEFAULT_MAX_QUESTS+";";
	}
}
