package de.westnordost.osmagent.quests.osm.persist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.AQuestDao;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.osm.changes.StringMapChanges;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.Element;

public class OsmQuestDao extends AQuestDao<OsmQuest>
{
	private final Serializer serializer;
	private final String questTypePackage;

	@Inject public OsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer,
							   String questTypePackage)
	{
		super(dbHelper);
		this.serializer = serializer;
		this.questTypePackage = questTypePackage;
	}

	@Override protected String getTableName()
	{
		return OsmQuestTable.NAME;
	}

	@Override protected String getMergedViewName()
	{
		return OsmQuestTable.NAME_MERGED_VIEW;
	}

	@Override protected String getIdColumnName()
	{
		return OsmQuestTable.Columns.QUEST_ID;
	}

	@Override protected String getLatitudeColumnName()
	{
		return ElementGeometryTable.Columns.LATITUDE;
	}

	@Override protected String getLongitudeColumnName()
	{
		return ElementGeometryTable.Columns.LONGITUDE;
	}

	@Override protected String getQuestStatusColumnName()
	{
		return OsmQuestTable.Columns.QUEST_STATUS;
	}

	@Override protected ContentValues createContentValuesFrom(OsmQuest quest)
	{
		ContentValues values = createNonFinalContentValuesFrom(quest);
		values.put(OsmQuestTable.Columns.QUEST_TYPE, quest.getType().getClass().getSimpleName());
		values.put(OsmQuestTable.Columns.ELEMENT_ID, quest.getElementId());
		values.put(OsmQuestTable.Columns.ELEMENT_TYPE, quest.getElementType().name());
		return values;
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(OsmQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(OsmQuestTable.Columns.QUEST_STATUS, quest.getStatus().name());
		values.put(OsmQuestTable.Columns.LAST_UPDATE, quest.getLastUpdate().getTime());

		if(quest.getChanges() != null)
		{
			values.put(OsmQuestTable.Columns.TAG_CHANGES, serializer.toBytes(quest.getChanges()));
		}

		return values;
	}

	@Override protected OsmQuest createObjectFrom(Cursor cursor)
	{
		int colQuestId = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.QUEST_ID),
			colElementId = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.ELEMENT_ID),
			colElementType = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.ELEMENT_TYPE),
			colQuestStatus = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.QUEST_STATUS),
			colQuestType = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.QUEST_TYPE),
			colChanges = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.TAG_CHANGES),
			colLastChange = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.LAST_UPDATE);

		long questId = cursor.getLong(colQuestId);
		long elementId = cursor.getLong(colElementId);

		Element.Type elementType = Element.Type.valueOf(cursor.getString(colElementType));
		QuestStatus questStatus = QuestStatus.valueOf(cursor.getString(colQuestStatus));
		OsmElementQuestType questType = getQuestTypeByName(cursor.getString(colQuestType));

		StringMapChanges changes = null;
		if(!cursor.isNull(colChanges))
		{
			changes = serializer.toObject(cursor.getBlob(colChanges), StringMapChanges.class);
		}

		ElementGeometry geometry = ElementGeometryDao.createObjectFrom(serializer, cursor);

		Date lastChange = new Date(cursor.getLong(colLastChange));

		return new OsmQuest(questId, questType, elementType, elementId, questStatus, changes,
				lastChange, geometry);
	}

	private OsmElementQuestType getQuestTypeByName(String name)
	{
		String pck = questTypePackage + "." + name;
		try
		{
			return (OsmElementQuestType) Class.forName(pck).newInstance();
		}
		catch (Exception e)
		{
			// if the class does not exist or is not instantiable, it is a programming error
			throw new RuntimeException(e);
		}
	}
}
