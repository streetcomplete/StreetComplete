package de.westnordost.streetcomplete.data.osmnotes;

public class NoteTable
{
	public static final String NAME = "osm_notes";

	public static class Columns
	{
		public static final String
				ID = "note_id",
				LATITUDE = "latitude",
				LONGITUDE = "longitude",
				STATUS = "note_status",
				CREATED  = "note_created",
				CLOSED = "note_closed",
				COMMENTS = "comments";
	}
}
