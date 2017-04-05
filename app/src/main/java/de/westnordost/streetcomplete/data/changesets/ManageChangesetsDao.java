package de.westnordost.streetcomplete.data.changesets;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/** Keep track of changesets and the date of the last change that has been made to them */
public class ManageChangesetsDao
{
	private final SQLiteOpenHelper dbHelper;
	private final SQLiteStatement upsertChangesetId, upsertLastChanged;

	@Inject
	public ManageChangesetsDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String insertOrReplace = "INSERT OR REPLACE INTO " + ManageChangesetsTable.NAME + " ("+
				ManageChangesetsTable.Columns.QUEST_TYPE+","+
				ManageChangesetsTable.Columns.CHANGESET_ID+","+
				ManageChangesetsTable.Columns.LAST_CHANGE+
				") values ";

		// params = questType, changesetId, questType
		upsertChangesetId = db.compileStatement(insertOrReplace + "(?,?," +
				getSelectString( ManageChangesetsTable.Columns.LAST_CHANGE) +
				");");
		// params = questType, questType, lastChange
		upsertLastChanged = db.compileStatement(insertOrReplace + "(?," +
				getSelectString( ManageChangesetsTable.Columns.CHANGESET_ID) +
				",?);");
	}

	private static String getSelectString(String column)
	{
		return "( SELECT " + column +
				" FROM " + ManageChangesetsTable.NAME +
				" WHERE " + ManageChangesetsTable.Columns.QUEST_TYPE + " = ?)";
	}

	public void setLastChangedToNow(String questType)
	{
		synchronized (upsertLastChanged) // statements are not threadsafe
		{
			upsertLastChanged.bindString(1, questType);
			upsertLastChanged.bindString(2, questType);
			upsertLastChanged.bindLong(3, System.currentTimeMillis());
			upsertLastChanged.execute();
			upsertLastChanged.clearBindings();
		}
	}

	public void assignChangesetId(String questType, long changesetId)
	{
		synchronized (upsertChangesetId) // statements are not threadsafe
		{
			upsertChangesetId.bindString(1, questType);
			upsertChangesetId.bindLong(2, changesetId);
			upsertChangesetId.bindString(3, questType);
			upsertChangesetId.executeInsert();
			upsertChangesetId.clearBindings();
		}
	}

	public ManageChangesetInfo get(String questType)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(ManageChangesetsTable.NAME, null,
				ManageChangesetsTable.Columns.QUEST_TYPE + " = ?", new String[]{questType},
				null,null,null,"1");

		try
		{
			if(!cursor.moveToFirst()) return null; // nothing found for this quest type
			return createFromCursor(cursor);
		}
		finally
		{
			cursor.close();
		}
	}

	public Collection<ManageChangesetInfo> getAll()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// https://youtu.be/B1BdQcJ2ZYY?t=85
		Cursor cursor = db.query(ManageChangesetsTable.NAME, null, null, null, null, null, null, null);

		List<ManageChangesetInfo> result = new ArrayList<>();

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					result.add(createFromCursor(cursor));
					cursor.moveToNext();
				}
			}
			return result;
		}
		finally
		{
			cursor.close();
		}
	}

	private ManageChangesetInfo createFromCursor(Cursor cursor)
	{
		int colQuestType = cursor.getColumnIndexOrThrow(ManageChangesetsTable.Columns.QUEST_TYPE);
		int colChangesetId = cursor.getColumnIndexOrThrow(ManageChangesetsTable.Columns.CHANGESET_ID);
		int colLastChange = cursor.getColumnIndexOrThrow(ManageChangesetsTable.Columns.LAST_CHANGE);

		ManageChangesetInfo result = new ManageChangesetInfo();
		result.questType = cursor.getString(colQuestType);
		if(!cursor.isNull(colChangesetId))
		{
			result.changesetId = cursor.getLong(colChangesetId);
		}
		result.lastChanged = cursor.getLong(colLastChange);

		return result;
	}

	public boolean delete(String questType)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(ManageChangesetsTable.NAME,
				ManageChangesetsTable.Columns.QUEST_TYPE + " = ?", new String[]{questType}) == 1;
	}
}
