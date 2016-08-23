package de.westnordost.osmagent.quests.persist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.ElementGeometry;
import de.westnordost.osmagent.quests.OsmQuest;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.StringMapChanges;
import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.Element;

public class OsmQuestDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public OsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	public OsmQuest get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(OsmQuestTable.NAME_MERGED_VIEW,
				null, OsmQuestTable.Columns.QUEST_ID + " = " + id, null, null, null, null, null);

		if(!cursor.moveToFirst()) return null;

		OsmQuest result;
		try
		{
			result = getCurrent(cursor);
		}
		finally
		{
			cursor.close();
		}

		return result;
	}

	public List<OsmQuest> getAll()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(OsmQuestTable.NAME_MERGED_VIEW,
				null, null, null, null, null, null, null);

		List<OsmQuest> result = new ArrayList<>();

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					result.add(getCurrent(cursor));
					cursor.moveToNext();
				}
			}
		}
		finally
		{
			cursor.close();
		}


		return result;
	}

	public void update(OsmQuest quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rows = db.update(OsmQuestTable.NAME, getQuestNonFinalContentValues(quest),
				OsmQuestTable.Columns.QUEST_ID + " = " + quest.getId(), null);

		if(rows == 0)
		{
			throw new NullPointerException("Quest with the id " + quest.getId() + " does not exist.");
		}
	}

	public void delete(long id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(OsmQuestTable.NAME, OsmQuestTable.Columns.QUEST_ID + " = " + id, null);
	}

	public long add(OsmQuest quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		return db.insertOrThrow(OsmQuestTable.NAME, null, getQuestContentValues(quest));
	}

	private ContentValues getQuestContentValues(OsmQuest quest)
	{
		ContentValues values = getQuestNonFinalContentValues(quest);
		values.put(OsmQuestTable.Columns.QUEST_TYPE, quest.getType().getClass().getSimpleName());
		values.put(OsmQuestTable.Columns.ELEMENT_ID, quest.getElementId());
		values.put(OsmQuestTable.Columns.ELEMENT_TYPE, quest.getElementType().ordinal());
		return values;
	}

	private ContentValues getQuestNonFinalContentValues(OsmQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(OsmQuestTable.Columns.QUEST_STATUS, quest.getStatus().ordinal());

		if(quest.getChanges() != null)
		{
			values.put(OsmQuestTable.Columns.TAG_CHANGES, serializer.toBytes(quest.getChanges()));
		}

		return values;
	}

	private OsmQuest getCurrent(Cursor cursor)
	{
		int colQuestId = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.QUEST_ID),
			colElementId = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.ELEMENT_ID),
			colElementType = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.ELEMENT_TYPE),
			colQuestStatus = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.QUEST_STATUS),
			colQuestType = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.QUEST_TYPE),
			colChanges = cursor.getColumnIndexOrThrow(OsmQuestTable.Columns.TAG_CHANGES),
			colGeometry = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.ELEMENT_GEOMETRY);

		long questId = cursor.getLong(colQuestId);
		long elementId = cursor.getLong(colElementId);

		Element.Type elementType = Element.Type.values()[cursor.getInt(colElementType)];
		QuestStatus questStatus = QuestStatus.valueOf(cursor.getString(colQuestStatus));
		QuestType questType = getQuestTypeByName(cursor.getString(colQuestType));

		StringMapChanges changes = null;
		if(!cursor.isNull(colChanges))
		{
			changes = serializer.toObject(cursor.getBlob(colChanges), StringMapChanges.class);
		}

		ElementGeometry geometry = serializer.toObject(cursor.getBlob(colGeometry), ElementGeometry.class);

		return new OsmQuest(questId, questType, elementId, elementType, questStatus, changes, geometry);
	}

	private QuestType getQuestTypeByName(String name)
	{
		try
		{
			return (QuestType) Class.forName(name).newInstance();
		}
		catch (Exception e)
		{
			// if the class does not exist or is not instantiable, it is a programming error
			throw new RuntimeException(e);
		}
	}

}
