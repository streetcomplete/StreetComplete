package de.westnordost.osmagent.quests.osm.persist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestDao;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.osm.changes.StringMapChanges;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class OsmQuestDao extends QuestDao<OsmQuest>
{
	@Inject public OsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper, serializer);
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
				colLastChange = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.LAST_UPDATE),
				colOuterGeometry = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.GEOMETRY_OUTER),
				colInnerGeometry = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.GEOMETRY_INNER),
				colCenterLat = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.LATITUDE),
				colCenterLon = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.LONGITUDE);

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

		List<List<LatLon>> outer, inner = null;
		outer = serializer.toObject(cursor.getBlob(colOuterGeometry), List.class);
		if(!cursor.isNull(colInnerGeometry))
		{
			inner = serializer.toObject(cursor.getBlob(colInnerGeometry), List.class);
		}
		LatLon center = new OsmLatLon(cursor.getDouble(colCenterLat), cursor.getDouble(colCenterLon));
		ElementGeometry geometry = new ElementGeometry(outer, inner, center);

		Date lastChange = new Date(cursor.getLong(colLastChange));

		return new OsmQuest(questId, questType, elementType, elementId, questStatus, changes,
				lastChange, geometry);
	}

	private OsmElementQuestType getQuestTypeByName(String name)
	{
		try
		{
			return (OsmElementQuestType) Class.forName(name).newInstance();
		}
		catch (Exception e)
		{
			// if the class does not exist or is not instantiable, it is a programming error
			throw new RuntimeException(e);
		}
	}
}
