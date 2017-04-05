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
				OpenChangesetsTable.Columns.CHANGESET_ID+
				") values (?,?);");
	}

	public long getLastQuestSolvedTime()
	{
		return prefs.getLong(Prefs.LAST_SOLVED_QUEST_TIME, 0);
	}

	public void setLastQuestSolvedTimeToNow()
	{
		prefs.edit().putLong(Prefs.LAST_SOLVED_QUEST_TIME, System.currentTimeMillis()).apply();
	}

	public void replace(String questType, long changesetId)
	{
		synchronized (replace) // statements are not threadsafe
		{
			replace.bindString(1, questType);
			replace.bindLong(2, changesetId);
			replace.executeInsert();
			replace.clearBindings();
		}
	}

	public OpenChangesetInfo get(String questType)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(OpenChangesetsTable.NAME, null,
				OpenChangesetsTable.Columns.QUEST_TYPE + " = ?", new String[]{questType},
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
		int colChangesetId = cursor.getColumnIndexOrThrow(OpenChangesetsTable.Columns.CHANGESET_ID);

		OpenChangesetInfo result = new OpenChangesetInfo();
		result.questType = cursor.getString(colQuestType);
		result.changesetId = cursor.getLong(colChangesetId);
		return result;
	}

	public boolean delete(String questType)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(OpenChangesetsTable.NAME,
				OpenChangesetsTable.Columns.QUEST_TYPE + " = ?", new String[]{questType}) == 1;
	}
}
