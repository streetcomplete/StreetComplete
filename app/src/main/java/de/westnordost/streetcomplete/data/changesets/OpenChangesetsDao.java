package de.westnordost.streetcomplete.data.changesets;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Prefs;

/** Keep track of changesets and the date of the last change that has been made to them */
public class OpenChangesetsDao
{
	public static final int CLOSE_CHANGESETS_AFTER_INACTIVITY_OF = 1000*60*20; // 20min

	private final SQLiteOpenHelper dbHelper;
	private final SharedPreferences prefs;
	private final SQLiteStatement replace;

	@Inject
	public OpenChangesetsDao(SQLiteOpenHelper dbHelper, SharedPreferences prefs)
	{
		this.dbHelper = dbHelper;
		this.prefs = prefs;
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// params = questType, changesetId
		replace = db.compileStatement(
				"INSERT OR REPLACE INTO " + OpenChangesetsTable.NAME + " ("+
				OpenChangesetsTable.Columns.QUEST_TYPE+","+
				OpenChangesetsTable.Columns.SOURCE+","+
				OpenChangesetsTable.Columns.CHANGESET_ID+
				") values (?,?,?);");
	}

	public long getLastQuestSolvedTime()
	{
		return prefs.getLong(Prefs.LAST_SOLVED_QUEST_TIME, 0);
	}

	public void setLastQuestSolvedTimeToNow()
	{
		prefs.edit().putLong(Prefs.LAST_SOLVED_QUEST_TIME, System.currentTimeMillis()).apply();
	}

	public void replace(OpenChangesetKey key, long changesetId)
	{
		synchronized (replace) // statements are not threadsafe
		{
			replace.bindString(1, key.questType);
			replace.bindString(2, key.source);
			replace.bindLong(3, changesetId);
			replace.executeInsert();
			replace.clearBindings();
		}
	}

	public OpenChangesetInfo get(OpenChangesetKey key)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(OpenChangesetsTable.NAME, null,
				OpenChangesetsTable.Columns.QUEST_TYPE + " = ? AND " +
				OpenChangesetsTable.Columns.SOURCE + " = ?", new String[]{key.questType, key.source},
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

	public Collection<OpenChangesetInfo> getAll()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// https://youtu.be/B1BdQcJ2ZYY?t=85
		Cursor cursor = db.query(OpenChangesetsTable.NAME, null, null, null, null, null, null, null);

		List<OpenChangesetInfo> result = new ArrayList<>();

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

	private OpenChangesetInfo createFromCursor(Cursor cursor)
	{
		int colQuestType = cursor.getColumnIndexOrThrow(OpenChangesetsTable.Columns.QUEST_TYPE);
		int colSource = cursor.getColumnIndex(OpenChangesetsTable.Columns.SOURCE);
		int colChangesetId = cursor.getColumnIndexOrThrow(OpenChangesetsTable.Columns.CHANGESET_ID);

		return new OpenChangesetInfo(
				new OpenChangesetKey(cursor.getString(colQuestType), cursor.getString(colSource)),
				cursor.getLong(colChangesetId));
	}

	public boolean delete(OpenChangesetKey key)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(OpenChangesetsTable.NAME,
				OpenChangesetsTable.Columns.QUEST_TYPE + " = ? AND " +
				OpenChangesetsTable.Columns.SOURCE + " = ?", new String[]{key.questType, key.source}) == 1;
	}
}
