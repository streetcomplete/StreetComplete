package de.westnordost.streetcomplete.data.osm.persist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.AQuestDao;
import de.westnordost.streetcomplete.data.WhereSelectionBuilder;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.QuestTypes;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public class OsmQuestDao extends AQuestDao<OsmQuest>
{
	private final Serializer serializer;
	private final QuestTypes questTypeList;
	private final SQLiteStatement add, replace;

	@Inject public OsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer,
							   QuestTypes questTypeList)
	{
		super(dbHelper);
		this.serializer = serializer;
		this.questTypeList = questTypeList;
		String sql = OsmQuestTable.NAME + " ("+
				OsmQuestTable.Columns.QUEST_ID+","+
				OsmQuestTable.Columns.QUEST_TYPE+","+
				OsmQuestTable.Columns.QUEST_STATUS+","+
				OsmQuestTable.Columns.TAG_CHANGES+","+
				OsmQuestTable.Columns.CHANGES_SOURCE+","+
				OsmQuestTable.Columns.LAST_UPDATE+","+
				OsmQuestTable.Columns.ELEMENT_ID+","+
				OsmQuestTable.Columns.ELEMENT_TYPE+
				") values (?,?,?,?,?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		add = db.compileStatement("INSERT OR IGNORE INTO " + sql);
		replace = db.compileStatement("INSERT OR REPLACE INTO " + sql);
	}

	public List<OsmQuest> getAll(BoundingBox bbox, QuestStatus status, String questTypeName,
								 Element.Type elementType, Long elementId)
	{
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addBBox(bbox, qb);
		addQuestStatus(status, qb);
		addQuestType(questTypeName, qb);
		addElementType(elementType, qb);
		addElementId(elementId, qb);

		return getAllThings(getMergedViewName(), null, qb, new CreateFromCursor<OsmQuest>()
		{
			@Override public OsmQuest create(Cursor cursor)
			{
				return createObjectFrom(cursor);
			}
		});
	}

	public int deleteAll(QuestStatus status, long olderThan)
	{
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addQuestStatus(status, qb);
		qb.appendAnd(OsmQuestTable.Columns.LAST_UPDATE + " < ?", String.valueOf(olderThan));
		return deleteAllThings(getTableName(), qb);
	}

	private void addQuestType(String questTypeName, WhereSelectionBuilder builder)
	{
		if(questTypeName != null)
		{
			builder.appendAnd(OsmQuestTable.Columns.QUEST_TYPE + " = ?", questTypeName);
		}
	}

	private void addElementType(Element.Type elementType, WhereSelectionBuilder builder)
	{
		if(elementType != null)
		{
			String elementKeyName = elementType.name();
			builder.appendAnd(OsmQuestTable.Columns.ELEMENT_TYPE + " = ?", elementKeyName);
		}
	}

	private void addElementId(Long elementId, WhereSelectionBuilder builder)
	{
		if(elementId != null)
		{
			String elementIdStr = String.valueOf(elementId);
			builder.appendAnd(OsmQuestTable.Columns.ELEMENT_ID + " = ?", elementIdStr);
		}
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

	@Override protected synchronized long executeInsert(OsmQuest quest, boolean replace)
	{
		SQLiteStatement stmt = replace ? this.replace : this.add;

		if(quest.getId() != null)
		{
			stmt.bindLong(1, quest.getId());
		}
		else
		{
			stmt.bindNull(1);
		}

		stmt.bindString(2, quest.getType().getClass().getSimpleName());
		stmt.bindString(3, quest.getStatus().name());
		if(quest.getChanges() != null)
		{
			stmt.bindBlob(4, serializer.toBytes(quest.getChanges()));
		}
		else
		{
			stmt.bindNull(4);
		}

		if(quest.getChangesSource() != null) stmt.bindString(5, quest.getChangesSource());
		else                                 stmt.bindNull(5);

		stmt.bindLong(6, new Date().getTime());
		stmt.bindLong(7, quest.getElementId());
		stmt.bindString(8, quest.getElementType().name());

		long result = stmt.executeInsert();
		stmt.clearBindings();
		return result;
	}

	@Override protected ContentValues createFinalContentValuesFrom(OsmQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(OsmQuestTable.Columns.QUEST_TYPE, quest.getType().getClass().getSimpleName());
		values.put(OsmQuestTable.Columns.ELEMENT_ID, quest.getElementId());
		values.put(OsmQuestTable.Columns.ELEMENT_TYPE, quest.getElementType().name());
		return values;
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(OsmQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(OsmQuestTable.Columns.QUEST_STATUS, quest.getStatus().name());
		values.put(OsmQuestTable.Columns.LAST_UPDATE, new Date().getTime());

		if(quest.getChanges() != null)
		{
			values.put(OsmQuestTable.Columns.TAG_CHANGES, serializer.toBytes(quest.getChanges()));
		}
		if(quest.getChangesSource() != null)
		{
			values.put(OsmQuestTable.Columns.CHANGES_SOURCE, quest.getChangesSource());
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
			colChangesSource = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.CHANGES_SOURCE),
			colLastChange = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.LAST_UPDATE);

		long questId = cursor.getLong(colQuestId);
		long elementId = cursor.getLong(colElementId);

		Element.Type elementType = Element.Type.valueOf(cursor.getString(colElementType));
		QuestStatus questStatus = QuestStatus.valueOf(cursor.getString(colQuestStatus));

        String questTypeName = cursor.getString(colQuestType);
		OsmElementQuestType questType = (OsmElementQuestType) questTypeList.forName(questTypeName);
        if(questType == null) {
            throw new IllegalArgumentException("The quest type " + questTypeName + " does not exist!");
        }

		StringMapChanges changes = null;
		if(!cursor.isNull(colChanges))
		{
			changes = serializer.toObject(cursor.getBlob(colChanges), StringMapChanges.class);
		}
		String changesSource = null;
		if(!cursor.isNull(colChangesSource))
		{
			changesSource = cursor.getString(colChangesSource);
		}

		ElementGeometry geometry = ElementGeometryDao.createObjectFrom(serializer, cursor);

		Date lastChange = new Date(cursor.getLong(colLastChange));

		return new OsmQuest(questId, questType, elementType, elementId, questStatus, changes,
				changesSource, lastChange, geometry);
	}
}
