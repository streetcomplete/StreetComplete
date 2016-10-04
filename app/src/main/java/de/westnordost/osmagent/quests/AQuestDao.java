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
	private final SQLiteOpenHelper dbHelper;

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

	public List<T> getAll(BoundingBox bbox, QuestStatus status)
	{
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addBBox(bbox, qb);
		addQuestStatus(status, qb);

		return getAllThings(getMergedViewName(), null, qb, new CreateFromCursor<T>()
		{
			@Override public T create(Cursor cursor)
			{
				return createObjectFrom(cursor);
			}
		});
	}

	protected final void addBBox(BoundingBox bbox, WhereSelectionBuilder builder)
	{
		if(bbox != null)
		{
			builder.appendAnd("(" + getLatitudeColumnName() + " BETWEEN ? AND ?)",
					String.valueOf(bbox.getMinLatitude()),
					String.valueOf(bbox.getMaxLatitude()));
			builder.appendAnd("(" + getLongitudeColumnName() + " BETWEEN ? AND ?)",
					String.valueOf(bbox.getMinLongitude()),
					String.valueOf(bbox.getMaxLongitude()));
		}
	}

	protected final void addQuestStatus(QuestStatus status, WhereSelectionBuilder builder)
	{
		if(status != null)
		{
			builder.appendAnd(getQuestStatusColumnName() + " = ?", status.name());
		}
	}

	protected final <E> List<E> getAllThings(String tablename, String[] cols,
											 WhereSelectionBuilder query, CreateFromCursor<E> creator)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(tablename, cols, query.getWhere(), query.getArgs(),
				null, null, null, null);

		List<E> result = new ArrayList<>();

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					result.add(creator.create(cursor));
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

	protected interface CreateFromCursor<E>
	{
		E create(Cursor cursor);
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

	public boolean delete(long id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(getTableName(), getIdColumnName() + " = " + id, null) == 1;
	}

	/** Add given quest to DB and sets the quest's id after inserting it
	 * @return true if successfully inserted, false if quest already exists in DB (= not inserted) */
	public boolean add(T quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long rowId = db.insertWithOnConflict(getTableName(), null, createContentValuesFrom(quest),
				SQLiteDatabase.CONFLICT_IGNORE);

		boolean alreadyExists = rowId == -1;
		if(!alreadyExists)
		{
			quest.setId(rowId);
		}
		return !alreadyExists;
	}

	/** Add given quest to DB and sets the quest's id after inserting it. If the quest already
	 *  exists, replaces it with the given one. */
	public void replace(T quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long rowId = db.insertWithOnConflict(getTableName(), null, createContentValuesFrom(quest),
				SQLiteDatabase.CONFLICT_REPLACE);

		quest.setId(rowId);
	}

	private ContentValues createContentValuesFrom(T object)
	{
		ContentValues result = createFinalContentValuesFrom(object);
		result.putAll(createNonFinalContentValuesFrom(object));
		return result;
	}

	protected abstract String getTableName();
	protected abstract String getMergedViewName();
	protected abstract String getIdColumnName();
	protected abstract String getQuestStatusColumnName();

	protected abstract String getLatitudeColumnName();
	protected abstract String getLongitudeColumnName();

	protected abstract ContentValues createNonFinalContentValuesFrom(T object);
	protected abstract ContentValues createFinalContentValuesFrom(T object);
	protected abstract T createObjectFrom(Cursor cursor);
}
