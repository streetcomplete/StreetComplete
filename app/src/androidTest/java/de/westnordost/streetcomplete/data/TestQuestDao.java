package de.westnordost.streetcomplete.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.Date;

import de.westnordost.osmapi.map.data.OsmLatLon;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A simple implementation of AQuestDao in order to test the abstract class */
public class TestQuestDao extends AQuestDao<Quest>
{
	public static final String TESTDB = "testdb.db";

	private static final String
			TABLE_NAME = "test",
			MERGED_VIEW_NAME = "test_full",
			ID_COL = "id",
			QS_COL = "quest_status",
			LAT_COL = "lat",
			LON_COL = "lon",
			LAST_UPDATE_COL = "last_update";

	private SQLiteOpenHelper dbHelper;

	public TestQuestDao(SQLiteOpenHelper dbHelper)
	{
		super(dbHelper);
		this.dbHelper = dbHelper;
	}

	@Override protected String getTableName() { return TABLE_NAME; }
	@Override protected String getMergedViewName() { return TABLE_NAME; }
	@Override protected String getIdColumnName() { return ID_COL; }
	@Override protected String getQuestStatusColumnName() { return QS_COL; }
	@Override protected String getLastChangedColumnName() { return LAST_UPDATE_COL; }

	@Override protected String getLatitudeColumnName() { return LAT_COL; }
	@Override protected String getLongitudeColumnName() { return LON_COL; }

	@Override protected long executeInsert(Quest quest, boolean replace)
	{
		String orWhat = replace ? "REPLACE" : "IGNORE";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteStatement insert = db.compileStatement(
				"INSERT OR "+orWhat+" INTO " + TABLE_NAME +
						"("+ID_COL+","+QS_COL+","+LAT_COL+","+LON_COL+","+LAST_UPDATE_COL+
						") VALUES (?,?,?,?,?)");

		insert.bindLong(1, quest.getId());
		insert.bindString(2, quest.getStatus().name());
		insert.bindDouble(3, quest.getMarkerLocation().getLatitude());
		insert.bindDouble(4, quest.getMarkerLocation().getLongitude());
		insert.bindDouble(5, quest.getLastUpdate().getTime());

		return insert.executeInsert();
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(Quest quest)
	{
		ContentValues v = new ContentValues();
		v.put(QS_COL, quest.getStatus().name());
		v.put(LAST_UPDATE_COL, quest.getLastUpdate().getTime());
		return v;
	}

	@Override protected ContentValues createFinalContentValuesFrom(Quest quest)
	{
		ContentValues v = new ContentValues();
		v.put(ID_COL, quest.getId());
		v.put(LAT_COL, quest.getMarkerLocation().getLatitude());
		v.put(LON_COL, quest.getMarkerLocation().getLongitude());
		return v;
	}

	@Override protected Quest createObjectFrom(Cursor cursor)
	{
		return createQuest(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2),
				QuestStatus.valueOf(cursor.getString(3)), cursor.getInt(4));
	}

	public static Quest createQuest(long id, double lat, double lon, QuestStatus status, long time)
	{
		Quest quest = mock(Quest.class);
		when(quest.getStatus()).thenReturn(status);
		when(quest.getId()).thenReturn(id);
		when(quest.getMarkerLocation()).thenReturn(new OsmLatLon(lat,lon));
		when(quest.getLastUpdate()).thenReturn(new Date(time));
		return quest;
	}

	public static class TestDbHelper extends SQLiteOpenHelper
	{
		public TestDbHelper(Context context)
		{
			super(context, TESTDB, null, 1);
		}

		@Override public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE "+TABLE_NAME+" ( " +
					ID_COL+" int PRIMARY KEY, " +
					LAT_COL+" double, " +
					LON_COL+" double, " +
					QS_COL+" varchar(255), " +
					LAST_UPDATE_COL+ " int);");
		}

		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{

		}
	}
}