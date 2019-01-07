package de.westnordost.streetcomplete.data.statistics;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.changes.MapDataChangesHandler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.osmapi.changesets.ChangesetsDao;

public class QuestStatisticsDao
{
	private static final String NOTE = "NOTE";

	private final SQLiteOpenHelper dbHelper;
	private final ChangesetsDao changesetsDao;
	private final UserChangesetsDao userChangesetsDao;

	@Inject
	public QuestStatisticsDao(SQLiteOpenHelper dbHelper, ChangesetsDao changesetsDao)
	{
		this.dbHelper = dbHelper;
		this.changesetsDao = changesetsDao;
		this.userChangesetsDao = new UserChangesetsDao(changesetsDao);
	}

	public void syncFromOsmServer(long userId)
	{
		final Map<String, Integer> data = new HashMap<>();

		userChangesetsDao.findAll(changeset ->
		{
			if(changeset.tags == null) return;

			String userAgent = changeset.tags.get("created_by");
			if(userAgent == null) return;

			if(!userAgent.startsWith(ApplicationConstants.NAME)) return;

			String questType = changeset.tags.get(ApplicationConstants.QUESTTYPE_TAG_KEY);
			if(questType == null) return;

			int prev = data.get(questType) != null ? data.get(questType) : 0;

			MapDataChangesCounter counter = new MapDataChangesCounter();
			changesetsDao.getData(changeset.id, counter);

			data.put(questType, prev + counter.count );
		}, userId, ApplicationConstants.DATE_OF_BIRTH);

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

	private static class MapDataChangesCounter implements MapDataChangesHandler
	{
		int count = 0;

		@Override public void onStartCreations() {}
		@Override public void onStartModifications() {}
		@Override public void onStartDeletions() {}
		@Override public void handle(BoundingBox bounds) {}

		@Override public void handle(Node node) { count++; }
		@Override public void handle(Way way) { count++; }
		@Override public void handle(Relation relation) { count++; }
	}

	public void addOneNote() { addOne(NOTE); }

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

	public int getNoteAmount() { return getAmount(NOTE); }

	public int getAmount(String questType)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String[] cols = new String[]{QuestStatisticsTable.Columns.SUCCEEDED};
		String where = QuestStatisticsTable.Columns.QUEST_TYPE + " = ?";
		String[] args = new String[]{questType};

		try (Cursor cursor = db.query(QuestStatisticsTable.NAME, cols, where, args, null, null, null, "1"))
		{
			if (!cursor.moveToFirst()) return 0;
			return cursor.getInt(0);
		}
	}

	public int getTotalAmount()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String[] cols = {"total("+QuestStatisticsTable.Columns.SUCCEEDED+")"};

		try (Cursor cursor = db.query(QuestStatisticsTable.NAME, cols, null, null, null, null, null, null))
		{
			if (!cursor.moveToFirst()) return 0;
			return cursor.getInt(0);
		}
	}
}
