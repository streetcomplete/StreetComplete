package de.westnordost.streetcomplete.data.osm.persist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.AQuestDao;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.WhereSelectionBuilder;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.util.Serializer;

import static de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns;

public abstract class AOsmQuestDao extends AQuestDao<OsmQuest>
{
	private final Serializer serializer;
	private final QuestTypeRegistry questTypeRegistry;
	private final SQLiteStatement add, replace;

	public AOsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer,
								QuestTypeRegistry questTypeRegistry)
	{
		super(dbHelper);
		this.serializer = serializer;
		this.questTypeRegistry = questTypeRegistry;
		String sql = getTableName() + " ("+
				Columns.QUEST_ID+","+
				Columns.QUEST_TYPE+","+
				Columns.QUEST_STATUS+","+
				Columns.TAG_CHANGES+","+
				Columns.CHANGES_SOURCE+","+
				Columns.LAST_UPDATE+","+
				Columns.ELEMENT_ID+","+
				Columns.ELEMENT_TYPE+
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

		return getAllThings(getMergedViewName(), null, qb, this::createObjectFrom);
	}

	public List<OsmQuest> getAll(BoundingBox bbox, QuestStatus status, List<String> questTypesNames)
	{
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addBBox(bbox, qb);
		addQuestStatus(status, qb);
		addQuestTypes(questTypesNames, qb);

		return getAllThings(getMergedViewName(), null, qb, this::createObjectFrom);
	}

	public int deleteAllReverted(Element.Type type, long id)
	{
		String query = getQuestStatusColumnName() + " = ? AND " +
				Columns.ELEMENT_TYPE + " = ? AND " + Columns.ELEMENT_ID + " = ?";

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(getTableName(), query,
				new String[] {QuestStatus.REVERT.name(), type.name(), String.valueOf(id)});
	}

	private void addQuestType(String questTypeName, WhereSelectionBuilder builder)
	{
		if(questTypeName != null)
		{
			builder.appendAnd(Columns.QUEST_TYPE + " = ?", questTypeName);
		}
	}

	protected final void addQuestTypes(List<String> questTypeNames, WhereSelectionBuilder builder)
	{
		if(questTypeNames != null)
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String questTypeName : questTypeNames)
			{
				if(first) first = false;
				else sb.append(",");
				sb.append("\"").append(questTypeName).append("\"");
			}
			builder.appendAnd(Columns.QUEST_TYPE + " IN (" + sb.toString() + ")");
		}
	}

	protected final void addElementType(Element.Type elementType, WhereSelectionBuilder builder)
	{
		if(elementType != null)
		{
			String elementKeyName = elementType.name();
			builder.appendAnd(Columns.ELEMENT_TYPE + " = ?", elementKeyName);
		}
	}

	protected final void addElementId(Long elementId, WhereSelectionBuilder builder)
	{
		if(elementId != null)
		{
			String elementIdStr = String.valueOf(elementId);
			builder.appendAnd(Columns.ELEMENT_ID + " = ?", elementIdStr);
		}
	}

	@Override protected String getIdColumnName() { return Columns.QUEST_ID; }
	@Override protected String getLatitudeColumnName() { return ElementGeometryTable.Columns.LATITUDE; }
	@Override protected String getLongitudeColumnName() { return ElementGeometryTable.Columns.LONGITUDE; }
	@Override protected String getQuestStatusColumnName() { return Columns.QUEST_STATUS; }
	@Override protected String getLastChangedColumnName() {	return Columns.LAST_UPDATE; }

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
		values.put(Columns.QUEST_TYPE, quest.getType().getClass().getSimpleName());
		values.put(Columns.ELEMENT_ID, quest.getElementId());
		values.put(Columns.ELEMENT_TYPE, quest.getElementType().name());
		return values;
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(OsmQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(Columns.QUEST_STATUS, quest.getStatus().name());
		values.put(Columns.LAST_UPDATE, new Date().getTime());

		StringMapChanges changes = quest.getChanges();
		values.put(Columns.TAG_CHANGES, changes != null ? serializer.toBytes(changes) : null);
		values.put(Columns.CHANGES_SOURCE, quest.getChangesSource());

		return values;
	}

	@Override protected OsmQuest createObjectFrom(Cursor cursor)
	{
		int colQuestId = cursor.getColumnIndexOrThrow(Columns.QUEST_ID),
			colElementId = cursor.getColumnIndexOrThrow(Columns.ELEMENT_ID),
			colElementType = cursor.getColumnIndexOrThrow(Columns.ELEMENT_TYPE),
			colQuestStatus = cursor.getColumnIndexOrThrow(Columns.QUEST_STATUS),
			colQuestType = cursor.getColumnIndexOrThrow(Columns.QUEST_TYPE),
			colChanges = cursor.getColumnIndexOrThrow(Columns.TAG_CHANGES),
			colChangesSource = cursor.getColumnIndexOrThrow(Columns.CHANGES_SOURCE),
			colLastChange = cursor.getColumnIndexOrThrow(Columns.LAST_UPDATE);

		long questId = cursor.getLong(colQuestId);
		long elementId = cursor.getLong(colElementId);

		Element.Type elementType = Element.Type.valueOf(cursor.getString(colElementType));
		QuestStatus questStatus = QuestStatus.valueOf(cursor.getString(colQuestStatus));

        String questTypeName = cursor.getString(colQuestType);
		OsmElementQuestType questType = (OsmElementQuestType) questTypeRegistry.getByName(questTypeName);
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
