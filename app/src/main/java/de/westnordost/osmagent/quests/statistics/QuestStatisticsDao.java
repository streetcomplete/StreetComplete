package de.westnordost.osmagent.quests.statistics;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestType;

public class QuestStatisticsDao
{
	private SQLiteOpenHelper dbHelper;

	@Inject
	public QuestStatisticsDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
	}

	public void addOne(QuestType questType)
	{
		String questTypeName = questType.getClass().getSimpleName();

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// first ensure the row exists
		ContentValues values = new ContentValues();
		values.put(QuestStatisticsTable.Columns.QUEST_TYPE, questTypeName);
		values.put(QuestStatisticsTable.Columns.SUCCEEDED, 0);
		db.insertWithOnConflict(QuestStatisticsTable.NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

		// then increase by one
		String[] args = {questTypeName};
		db.execSQL("UPDATE " + QuestStatisticsTable.NAME + " SET " +
				QuestStatisticsTable.Columns.SUCCEEDED + " = " + QuestStatisticsTable.Columns.SUCCEEDED +
				" + 1 WHERE " + QuestStatisticsTable.Columns.QUEST_TYPE + " = ?", args);
	}

	public int getAmount(QuestType questType)
	{
		String questTypeName = questType.getClass().getSimpleName();

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(QuestStatisticsTable.NAME,
				new String[]{QuestStatisticsTable.Columns.SUCCEEDED},
				QuestStatisticsTable.Columns.QUEST_TYPE + " = ?",
				new String[]{questTypeName},
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
