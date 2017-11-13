package de.westnordost.streetcomplete.data.osm.persist;

import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.util.Serializer;

public class OsmQuestDao extends AOsmQuestDao
{

	@Inject public OsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer, QuestTypeRegistry questTypeList)
	{
		super(dbHelper, serializer, questTypeList);
	}

	@Override protected String getTableName() { return OsmQuestTable.NAME; }
	@Override protected String getMergedViewName() { return OsmQuestTable.NAME_MERGED_VIEW; }
}
