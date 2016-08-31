package de.westnordost.osmagent.quests.osm.persist;

public class ElementGeometryTable
{
	public static final String NAME = "elements_geometry";

	public static class Columns
	{
		public static final String
				ELEMENT_ID = OsmQuestTable.Columns.ELEMENT_ID,
				ELEMENT_TYPE  = OsmQuestTable.Columns.ELEMENT_TYPE,
				GEOMETRY_OUTER = "geometry_outer",
				GEOMETRY_INNER = "geometry_inner",
				LATITUDE = "latitude",
				LONGITUDE = "longitude";
	}
}
