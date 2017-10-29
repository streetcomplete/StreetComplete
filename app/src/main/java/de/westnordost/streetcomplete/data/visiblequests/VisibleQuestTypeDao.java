package de.westnordost.streetcomplete.data.visiblequests;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;

public class VisibleQuestTypeDao
{
	private final SQLiteOpenHelper dbHelper;
	private final QuestTypeRegistry questTypeRegistry;
	private final SQLiteStatement replaceVisibility;

	@Inject public VisibleQuestTypeDao(SQLiteOpenHelper dbHelper, QuestTypeRegistry questTypeRegistry)
	{
		this.dbHelper = dbHelper;
		this.questTypeRegistry = questTypeRegistry;

		replaceVisibility = dbHelper.getWritableDatabase().compileStatement(
				"INSERT OR REPLACE INTO " + QuestVisibilityTable.NAME + " ("+
						QuestVisibilityTable.Columns.QUEST_TYPE+","+
						QuestVisibilityTable.Columns.VISIBILITY+
						") values (?,?);");
	}

	/** @return all visible quests ordered by importance */
	public List<QuestType> getAll()
	{
		List<QuestType> questTypes = new ArrayList<>(questTypeRegistry.getAll());

		Map<String, Boolean> questTypeVisibilities = getQuestTypeVisibilities();

		Iterator<QuestType> it = questTypes.listIterator();
		while(it.hasNext())
		{
			QuestType questType = it.next();
			Boolean isVisible = questTypeVisibilities.get(questType.getClass().getSimpleName());
			if(isVisible == null)
			{
				isVisible = questType.isDefaultEnabled();
			}

			if(!isVisible)
			{
				it.remove();
			}
		}

		return questTypes;
	}

	private Map<String, Boolean> getQuestTypeVisibilities()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(QuestVisibilityTable.NAME, null, null,null, null, null, null, null);

		Map<String, Boolean> result = new HashMap<>();

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
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
		finally
		{
			cursor.close();
		}
	}

	public void setVisible(QuestType questType, boolean visible)
	{
		synchronized (replaceVisibility)
		{
			replaceVisibility.bindString(1, questType.getClass().getSimpleName());
			replaceVisibility.bindLong(2, visible ? 1 : 0);
			replaceVisibility.executeInsert();
			replaceVisibility.clearBindings();
		}
	}
}
