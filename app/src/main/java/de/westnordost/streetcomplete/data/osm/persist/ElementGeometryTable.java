package de.westnordost.streetcomplete.data.osm.persist;

public class ElementGeometryTable
{
	public static final String NAME = "elements_geometry";

	public static class Columns
	{
		public static final String
				ELEMENT_ID = OsmQuestTable.Columns.ELEMENT_ID,
				ELEMENT_TYPE  = OsmQuestTable.Columns.ELEMENT_TYPE,
				GEOMETRY_POLYGONS = "geometry_polygons",
				GEOMETRY_POLYLINES = "geometry_polylines",
				LATITUDE = "latitude",
				LONGITUDE = "longitude";
	}
}
