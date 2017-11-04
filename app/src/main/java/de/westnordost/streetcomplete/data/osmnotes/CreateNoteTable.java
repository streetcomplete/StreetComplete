package de.westnordost.streetcomplete.data.osmnotes;

public class CreateNoteTable
{
	public static final String NAME = "osm_create_notes";

	public static class Columns
	{
		public static final String
				ID = "create_id",
				LATITUDE = "latitude",
				LONGITUDE = "longitude",
				TEXT = "text",
				ELEMENT_TYPE = "element_type",
				ELEMENT_ID = "element_id",
				QUEST_TITLE = "quest_title",
				IMAGE_PATHS = "image_paths";
	}
}
