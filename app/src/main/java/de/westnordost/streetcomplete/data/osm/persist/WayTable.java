package de.westnordost.streetcomplete.data.osm.persist;

public class WayTable
{
	public static final String NAME = "osm_ways";

	public static class Columns
	{
		public static final String
				ID = "id",
				VERSION  = "version",
				TAGS = "tags",
				NODE_IDS = "node_ids";
	}
}
