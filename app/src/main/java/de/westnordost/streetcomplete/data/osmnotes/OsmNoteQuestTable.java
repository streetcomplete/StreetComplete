package de.westnordost.streetcomplete.data.osmnotes;

public class OsmNoteQuestTable
{
	public static final String NAME = "osm_notequests";

	public static final String NAME_MERGED_VIEW = "osm_notequests_full";

	public static class Columns
	{
		public static final String
				QUEST_ID = "quest_id",
				NOTE_ID = NoteTable.Columns.ID,
				QUEST_STATUS = "quest_status",
				COMMENT = "changes",
				LAST_UPDATE = "last_update",
				IMAGE_PATHS = "image_paths";
	}
}
