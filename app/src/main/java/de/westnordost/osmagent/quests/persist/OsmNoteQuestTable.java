package de.westnordost.osmagent.quests.persist;

public class OsmNoteQuestTable
{
	public static final String NAME = "osm_notequests";

	public static final String NAME_MERGED_VIEW = "osm_notequests_full";

	public static class Columns
	{
		public static final String
				NOTE_ID = "note_id",
				QUEST_STATUS = "quest_status",
				CHANGES = "changes";
	}
}
