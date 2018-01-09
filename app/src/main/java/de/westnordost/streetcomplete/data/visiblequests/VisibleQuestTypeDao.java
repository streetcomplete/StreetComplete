package de.westnordost.streetcomplete.data.visiblequests;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestType;

public class VisibleQuestTypeDao
{
	private final SQLiteOpenHelper dbHelper;
	private final SQLiteStatement replaceVisibility;

	private Map<String, Boolean> questTypeVisibilities;

	@Inject public VisibleQuestTypeDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
		questTypeVisibilities = null;

		replaceVisibility = dbHelper.getWritableDatabase().compileStatement(
				"INSERT OR REPLACE INTO " + QuestVisibilityTable.NAME + " ("+
						QuestVisibilityTable.Columns.QUEST_TYPE+","+
						QuestVisibilityTable.Columns.VISIBILITY+
						") values (?,?);");

	}

	private Map<String, Boolean> getQuestTypeVisibilities()
	{
		if(questTypeVisibilities == null)
		{
			questTypeVisibilities = loadQuestTypeVisibilities();
		}
		return questTypeVisibilities;
	}

	private Map<String, Boolean> loadQuestTypeVisibilities()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Map<String, Boolean> result = new HashMap<>();

		try (Cursor cursor = db.query(QuestVisibilityTable.NAME, null, null, null, null, null, null, null))
		{
			if (cursor.moveToFirst())
			{
				while (!cursor.isAfterLast())
				{
					int colQuestType = cursor.getColumnIndexOrThrow(QuestVisibilityTable.Columns.QUEST_TYPE);
					int colVisibility = cursor.getColumnIndex(QuestVisibilityTable.Columns.VISIBILITY);

					String questTypeName = cursor.getString(colQuestType);
					boolean visible = cursor.getInt(colVisibility) != 0;

					result.put(questTypeName, visible);
					cursor.moveToNext();
				}
			}
			return result;
		}
	}

	public synchronized boolean isVisible(QuestType questType)
	{
		String questTypeName = questType.getClass().getSimpleName();
		Boolean isVisible = getQuestTypeVisibilities().get(questTypeName);
		if(isVisible == null)
		{
			isVisible = questType.getDefaultDisabledMessage() <= 0;
		}
		return isVisible;
	}

	public synchronized void setVisible(QuestType questType, boolean visible)
	{
		String questTypeName = questType.getClass().getSimpleName();
		synchronized (replaceVisibility)
		{
			replaceVisibility.bindString(1, questTypeName);
			replaceVisibility.bindLong(2, visible ? 1 : 0);
			replaceVisibility.executeInsert();
			replaceVisibility.clearBindings();
		}
		// update cache
		if(questTypeVisibilities != null)
		{
			questTypeVisibilities.put(questTypeName, visible);
		}
	}

	public synchronized void clear()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + QuestVisibilityTable.NAME);
		questTypeVisibilities = null;
	}
}
