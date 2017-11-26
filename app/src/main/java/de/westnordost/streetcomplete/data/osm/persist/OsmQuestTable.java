package de.westnordost.streetcomplete.data.osm.persist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OsmQuestTable
{
	public static final String NAME = "osm_quests";
	public static final String NAME_MERGED_VIEW = "osm_quests_full";

	public static final String NAME_UNDO = "osm_quests_undo";
	public static final String NAME_UNDO_MERGED_VIEW = "osm_quests_full_undo";


	public static class Columns
	{
		public static final String
				QUEST_ID = "quest_id",
				QUEST_TYPE = "quest_type",
				ELEMENT_ID = "element_id",
				ELEMENT_TYPE  = "element_type",
				QUEST_STATUS = "quest_status",
				TAG_CHANGES = "tag_changes",
				CHANGES_SOURCE = "changes_source",
				LAST_UPDATE = "last_update";

		public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
				QUEST_ID, QUEST_TYPE, ELEMENT_ID, ELEMENT_TYPE, QUEST_STATUS, TAG_CHANGES,
				CHANGES_SOURCE, LAST_UPDATE));

		public static final List<String> ALL_DB_VERSION_3 = Collections.unmodifiableList(Arrays.asList(
				QUEST_ID, QUEST_TYPE, ELEMENT_ID, ELEMENT_TYPE, QUEST_STATUS, TAG_CHANGES,
				LAST_UPDATE));
	}
}
