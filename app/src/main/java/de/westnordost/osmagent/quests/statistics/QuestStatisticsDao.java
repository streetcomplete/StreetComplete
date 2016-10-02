package de.westnordost.osmagent.quests.statistics;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.quests.QuestType;
import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.changesets.QueryChangesetsFilters;
import de.westnordost.osmapi.common.Handler;

public class QuestStatisticsDao
{
	private final SQLiteOpenHelper dbHelper;
	private final ChangesetsDao changesetsDao;

	@Inject
	public QuestStatisticsDao(SQLiteOpenHelper dbHelper, ChangesetsDao changesetsDao)
	{
		this.dbHelper = dbHelper;
		this.changesetsDao = changesetsDao;
	}

	public void syncFromOsmServer(long userId)
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters().byUser(userId).onlyClosed();
		final Map<String, Integer> data = new HashMap<>();

		changesetsDao.find(new Handler<ChangesetInfo>()
		{
			@Override public void handle(ChangesetInfo changeset)
			{
				if(changeset.tags == null) return;

				String userAgent = changeset.tags.get("created_by");
				if(!userAgent.startsWith(OsmagentConstants.NAME)) return;

				String questType = changeset.tags.get(OsmagentConstants.QUESTTYPE_TAG_KEY);
				if(questType == null) return;

				int prev = data.get(questType) != null ? data.get(questType) : 0;
				data.put(questType, prev+1);
			}
		}, filters);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// clear table
		db.delete(QuestStatisticsTable.NAME, null, null);

		for(Map.Entry<String, Integer> dataForQuestType : data.entrySet())
		{
			ContentValues values = new ContentValues(2);
			values.put(QuestStatisticsTable.Columns.QUEST_TYPE, dataForQuestType.getKey());
			values.put(QuestStatisticsTable.Columns.SUCCEEDED, dataForQuestType.getValue());
			db.insert(QuestStatisticsTable.NAME, null, values);
		}
	}

	public void addOne(String questType)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// first ensure the row exists
		ContentValues values = new ContentValues();
		values.put(QuestStatisticsTable.Columns.QUEST_TYPE, questType);
		values.put(QuestStatisticsTable.Columns.SUCCEEDED, 0);
		db.insertWithOnConflict(QuestStatisticsTable.NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

		// then increase by one
		String[] args = {questType};
		db.execSQL("UPDATE " + QuestStatisticsTable.NAME + " SET " +
				QuestStatisticsTable.Columns.SUCCEEDED + " = " + QuestStatisticsTable.Columns.SUCCEEDED +
				" + 1 WHERE " + QuestStatisticsTable.Columns.QUEST_TYPE + " = ?", args);
	}

	public int getAmount(String questType)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(QuestStatisticsTable.NAME,
				new String[]{QuestStatisticsTable.Columns.SUCCEEDED},
				QuestStatisticsTable.Columns.QUEST_TYPE + " = ?",
				new String[]{questType},
				null, null, null, "1");

		try
		{
			if(!cursor.moveToFirst()) return 0;
			return cursor.getInt(0);
		}
		finally
		{
			cursor.close();
		}
	}

	public int getTotalAmount()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String[] cols = {"total("+QuestStatisticsTable.Columns.SUCCEEDED+")"};
		Cursor cursor = db.query(QuestStatisticsTable.NAME, cols, null,	null, null, null, null, null);

		try
		{
			if(!cursor.moveToFirst()) return 0;
			return cursor.getInt(0);
		}
		finally
		{
			cursor.close();
		}
	}
}
