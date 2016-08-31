package de.westnordost.osmagent.quests.statistics;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestType;
import de.westnordost.osmagent.util.Serializer;

public class QuestStatisticsDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public QuestStatisticsDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	public void increase(QuestType questType)
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

}
