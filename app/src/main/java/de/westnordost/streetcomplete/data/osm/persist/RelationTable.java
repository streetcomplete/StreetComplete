package de.westnordost.streetcomplete.data.osm.persist;

public class RelationTable
{
	public static final String NAME = "osm_relations";

	public static class Columns
	{
		public static final String
				ID = "id",
				VERSION  = "version",
				TAGS = "tags",
				MEMBERS = "members";
	}
}
