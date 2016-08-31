package de.westnordost.osmagent.quests.osmnotes;

import de.westnordost.osmagent.quests.osmnotes.NoteTable;

public class OsmNoteQuestTable
{
	public static final String NAME = "osm_notequests";

	public static final String NAME_MERGED_VIEW = "osm_notequests_full";

	public static class Columns
	{
		public static final String
				NOTE_ID = NoteTable.Columns.ID,
				QUEST_STATUS = "quest_status",
				CHANGES = "changes",
				LAST_UPDATE = "last_update";
	}
}
