package de.westnordost.osmagent.quests;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;

public abstract class AQuestDao<T extends Quest>
{
	private SQLiteOpenHelper dbHelper;

	public AQuestDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
	}

	public T get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(getMergedViewName(), null, getIdColumnName() + " = " + id,
				null, null, null, null, "1");

		try
		{
			if(!cursor.moveToFirst()) return null;
			return createObjectFrom(cursor);
		}
		finally
		{
			cursor.close();
		}
	}

	public List<Long> getIdsByStatus(QuestStatus status)
	{
		List<Long> result = new ArrayList<>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String[] cols = {getIdColumnName()};
		String[] args = {status.name()};
		Cursor cursor = db.query(getTableName(), cols, getQuestStatusColumnName() + " = ?", args,
				null, null, null, null);

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					result.add(cursor.getLong(0));
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

	public List<T> getAll(BoundingBox bbox, QuestStatus status)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String query = getQuestStatusColumnName() + " = ? AND " +
				"("+getLatitudeColumnName()+" BETWEEN ? AND ?) AND " +
				"("+getLongitudeColumnName()+" BETWEEN ? AND ?)";

		String[] args = {status.name(),
				String.valueOf(bbox.getMinLatitude()), String.valueOf(bbox.getMaxLatitude()),
				String.valueOf(bbox.getMinLongitude()), String.valueOf(bbox.getMaxLongitude())
		};

		Cursor cursor = db.query(getMergedViewName(), null, query, args, null, null, null, null);

		List<T> result = new ArrayList<>();

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					result.add(createObjectFrom(cursor));
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

	public void update(T quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rows = db.update(getTableName(), createNonFinalContentValuesFrom(quest),
				getIdColumnName() + " = " + quest.getId(), null);

		if(rows == 0)
		{
			throw new NullPointerException(quest.getClass().getSimpleName() + " with the id " +
					quest.getId() + " does not exist.");
		}
	}

	public int delete(long id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(getTableName(), getIdColumnName() + " = " + id, null);
	}

	public long add(T quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		return db.insertWithOnConflict(getTableName(), null, createContentValuesFrom(quest),
				SQLiteDatabase.CONFLICT_IGNORE);
	}

	protected abstract String getTableName();
	protected abstract String getMergedViewName();
	protected abstract String getIdColumnName();
	protected abstract String getQuestStatusColumnName();

	protected abstract String getLatitudeColumnName();
	protected abstract String getLongitudeColumnName();

	protected abstract ContentValues createNonFinalContentValuesFrom(T object);
	protected abstract ContentValues createContentValuesFrom(T object);
	protected abstract T createObjectFrom(Cursor cursor);
}
