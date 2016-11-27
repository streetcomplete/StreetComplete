package de.westnordost.streetcomplete.data.osm.persist;

public class NodeTable
{
	public static final String NAME = "osm_nodes";

	public static class Columns
	{
		public static final String
				ID = "id",
				LATITUDE = "latitude",
				LONGITUDE = "longitude",
				VERSION  = "version",
				TAGS = "tags";
	}
}
