package de.westnordost.osmagent.quests.persist;

public class ElementGeometryTable
{
	public static final String NAME = "elements_geometry";

	public static class Columns
	{
		public static final String
				ELEMENT_ID = OsmQuestTable.Columns.ELEMENT_ID,
				ELEMENT_TYPE  = OsmQuestTable.Columns.ELEMENT_TYPE,
				ELEMENT_GEOMETRY = "element_geometry";
	}
}
