package de.westnordost.streetcomplete.data.complete;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.AQuestDao;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.WhereSelectionBuilder;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable;
import de.westnordost.streetcomplete.util.Serializer;

public class CompleteQuestDao extends AQuestDao<CompleteQuest>
{
	protected final SQLiteOpenHelper dbHelper;
	private final Serializer serializer;
	private final QuestTypeRegistry questTypeRegistry;
	private final SQLiteStatement add, replace;

	@Inject public CompleteQuestDao(SQLiteOpenHelper dbHelper, QuestTypeRegistry questTypeRegistry,
									Serializer serializer)
	{
		super(dbHelper);
		this.dbHelper = dbHelper;
		this.questTypeRegistry = questTypeRegistry;
		this.serializer = serializer;

		String sql = CompleteQuestTable.NAME + " ("+
				CompleteQuestTable.Columns.QUEST_ID+","+
				CompleteQuestTable.Columns.QUEST_TYPE+","+
				CompleteQuestTable.Columns.API_ID+","+
				CompleteQuestTable.Columns.QUEST_STATUS+","+
				CompleteQuestTable.Columns.COUNTRY+","+
				CompleteQuestTable.Columns.COMPLETE_TYPE+","+
				CompleteQuestTable.Columns.ANSWER+","+
				CompleteQuestTable.Columns.LAST_UPDATE+","+
				CompleteQuestTable.Columns.ELEMENT_ID+","+
				CompleteQuestTable.Columns.ELEMENT_TYPE+
				") values (?,?,?,?,?,?,?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		add = db.compileStatement("INSERT OR IGNORE INTO " + sql);
		replace = db.compileStatement("INSERT OR REPLACE INTO " +sql);
	}

	public List<CompleteQuest> getAllByType(String questType, QuestStatus status)
	{
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		qb.appendAnd(CompleteQuestTable.Columns.QUEST_TYPE + " = ?", questType);
		if (status != null) addQuestStatus(status, qb);

		return getAllThings(getMergedViewName(), null, qb, this::createObjectFrom);
	}

	@Override protected String getTableName() { return CompleteQuestTable.NAME; }
	@Override protected String getMergedViewName() { return CompleteQuestTable.NAME_MERGED_VIEW; }
	@Override protected String getIdColumnName() { return CompleteQuestTable.Columns.QUEST_ID; }
	@Override protected String getLatitudeColumnName() { return CompleteQuestTable.Columns.LATITUDE; }
	@Override protected String getLongitudeColumnName()	{ return CompleteQuestTable.Columns.LONGITUDE; }
	@Override protected String getQuestStatusColumnName() { return CompleteQuestTable.Columns.QUEST_STATUS; }
	@Override protected String getLastChangedColumnName() {	return CompleteQuestTable.Columns.LAST_UPDATE; }

	@Override protected synchronized long executeInsert(CompleteQuest quest, boolean replace)
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
		stmt.bindLong(3, quest.getComplete().apiId);
		stmt.bindString(4, quest.getStatus().name());
		stmt.bindString(5, quest.getComplete().country);
		stmt.bindString(6, quest.getComplete().completeType);
		if(quest.getComplete().answer != null)
		{
			stmt.bindString(7, quest.getComplete().answer);
		}
		else
		{
			stmt.bindNull(7);
		}

		stmt.bindLong(8, new Date().getTime());

		stmt.bindLong(9, quest.getElementId());
		stmt.bindString(10, quest.getElementType().name());

		long result = stmt.executeInsert();
		stmt.clearBindings();

		return result;
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(CompleteQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(CompleteQuestTable.Columns.QUEST_TYPE, quest.getType().getClass().getSimpleName());
		values.put(CompleteQuestTable.Columns.API_ID, quest.getComplete().apiId);
		values.put(CompleteQuestTable.Columns.QUEST_STATUS, quest.getStatus().name());
		values.put(CompleteQuestTable.Columns.COUNTRY, quest.getComplete().country);
		values.put(CompleteQuestTable.Columns.COMPLETE_TYPE, quest.getComplete().completeType);
		if (quest.getComplete().answer != null) values.put(CompleteQuestTable.Columns.ANSWER, quest.getComplete().answer);
		values.put(CompleteQuestTable.Columns.LAST_UPDATE, quest.getLastUpdate().getTime());

		return values;
	}

	@Override protected ContentValues createFinalContentValuesFrom(CompleteQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(CompleteQuestTable.Columns.QUEST_ID, quest.getId());
		values.put(CompleteQuestTable.Columns.ELEMENT_ID, quest.getElementId());
		values.put(CompleteQuestTable.Columns.ELEMENT_TYPE, quest.getElementType().name());
		return values;
	}

	@Override protected CompleteQuest createObjectFrom(Cursor cursor)
	{
		int colId = cursor.getColumnIndexOrThrow(CompleteQuestTable.Columns.QUEST_ID),
				colQuestType = cursor.getColumnIndexOrThrow(CompleteQuestTable.Columns.QUEST_TYPE),
				colApiId = cursor.getColumnIndexOrThrow(CompleteQuestTable.Columns.API_ID),
				colCountry = cursor.getColumnIndexOrThrow(CompleteQuestTable.Columns.COUNTRY),
				colCompleteType = cursor.getColumnIndexOrThrow(CompleteQuestTable.Columns.COMPLETE_TYPE),
				colAnswer = cursor.getColumnIndexOrThrow(CompleteQuestTable.Columns.ANSWER),
				colElementId = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.ELEMENT_ID),
				colElementType = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.ELEMENT_TYPE);


		Long questId = cursor.getLong(colId);

		long elementId = cursor.getLong(colElementId);
		Element.Type elementType = Element.Type.valueOf(cursor.getString(colElementType));

		Complete complete = new Complete();
		complete.id = questId;
		complete.apiId = cursor.getInt(colApiId);
		complete.country = cursor.getString(colCountry);
		complete.completeType = cursor.getString(colCompleteType);
		complete.answer = cursor.getString(colAnswer);

		String questTypeName = cursor.getString(colQuestType);
		CompleteQuestType questType = (CompleteQuestType) questTypeRegistry.getByName(questTypeName);
		if(questType == null) {
			throw new IllegalArgumentException("The quest type " + questTypeName + " does not exist!");
		}

		ElementGeometry geometry = ElementGeometryDao.createObjectFrom(serializer, cursor);

		return new CompleteQuest(questId, complete, QuestStatus.NEW, new Date(), questType, elementType, elementId, geometry);
	}
}