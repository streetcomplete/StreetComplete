package de.westnordost.streetcomplete.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;

public abstract class AQuestDao<T extends Quest>
{
    private static final String TAG = "QuestDao";

	protected final SQLiteOpenHelper dbHelper;

	public AQuestDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
	}

	public T get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String where = getIdColumnName() + " = " + id;
		try (Cursor cursor = db.query(getMergedViewName(), null, where,	null, null, null, null, "1"))
		{
			if (!cursor.moveToFirst()) return null;
			return createObjectFrom(cursor);
		}
	}

	public List<T> getAll(BoundingBox bbox, QuestStatus status)
	{
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addBBox(bbox, qb);
		addQuestStatus(status, qb);

		return getAllThings(getMergedViewName(), null, qb, this::createObjectFrom);
	}

	public T getLastSolved()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String questStatus = getQuestStatusColumnName();
		String query = questStatus + " IN (?,?,?)";
		String[] args = {QuestStatus.HIDDEN.name(), QuestStatus.ANSWERED.name(), QuestStatus.CLOSED.name()};
		String orderBy = getLastChangedColumnName() + " DESC";

		try (Cursor cursor = db.query(getMergedViewName(), null, query, args, null, null, orderBy, "1"))
		{
			if (!cursor.moveToFirst()) return null;
			return createObjectFrom(cursor);
		}
	}

	public int getCount(BoundingBox bbox, QuestStatus status)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addBBox(bbox, qb);
		addQuestStatus(status, qb);

		try (Cursor cursor = db.query(getMergedViewName(), new String[]{"COUNT(*)"},
				qb.getWhere(), qb.getArgs(), null, null, null, null))
		{
			cursor.moveToFirst();
			return cursor.getInt(0);
		}
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

		List<E> result = new ArrayList<>();

        List<Long> invalidIds = new ArrayList<>();

		try (Cursor cursor = db.query(tablename, cols, query.getWhere(), query.getArgs(), null, null, null, null))
		{
			if (cursor.moveToFirst())
			{
				while (!cursor.isAfterLast())
				{
					try
					{
						result.add(creator.create(cursor));
					} catch (Exception e)
					{
						Log.e(TAG, "Getting quest from db caused an exception", e);
						int idCol = cursor.getColumnIndex(getIdColumnName());
						invalidIds.add(cursor.getLong(idCol));
					}
					cursor.moveToNext();
				}
			}
		}

		if(!invalidIds.isEmpty())
        {
            Log.i(TAG, "The previously encountered corrupt quests are now removed from database");
            deleteAll(invalidIds);
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

	public int deleteAll(Collection<Long> ids)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		StringBuilder idsString = new StringBuilder();
		boolean first = true;
		for (Long id : ids)
		{
			if(first) first = false;
			else idsString.append(",");
			idsString.append(id);
		}
		return db.delete(getTableName(), getIdColumnName() + " IN (" + idsString.toString() + ")", null);
	}

	public int deleteAllClosed(long olderThan)
	{
		String statusCol = getQuestStatusColumnName();
		String query = "(" + statusCol + " = ? OR " + statusCol + " = ?) AND " +
				getLastChangedColumnName() + " < ?";

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(getTableName(), query, new String[]{
				QuestStatus.CLOSED.name(), QuestStatus.REVERT.name(), String.valueOf(olderThan)});
	}

	public int addAll(Collection<T> quests)
	{
		return insertAll(quests, false);
	}

	public int replaceAll(Collection<T> quests)
	{
		return insertAll(quests, true);
	}

	private int insertAll(Collection<T> quests, boolean replace)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		int addRows = 0;
		db.beginTransaction();
		for(T quest : quests)
		{
			long rowId = executeInsert(quest, replace);
			boolean alreadyExists = rowId == -1;

			if(!alreadyExists)
			{
				quest.setId(rowId);
				addRows++;
			}
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		return addRows;
	}

	/** Add given quest to DB and sets the quest's id after inserting it
	 * @return true if successfully inserted, false if quest already exists in DB (= not inserted) */
	public boolean add(T quest)
	{
		return insert(quest, false);
	}

	/** Add given quest to DB and sets the quest's id after inserting it. If the quest already
	 *  exists, replaces it with the given one. */
	public boolean replace(T quest)
	{
		return insert(quest, true);
	}

	private boolean insert(T quest, boolean replace)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();

		long rowId = executeInsert(quest, replace);

		boolean alreadyExists = rowId == -1;

		db.setTransactionSuccessful();
		db.endTransaction();

		if(!alreadyExists)
		{
			quest.setId(rowId);
		}
		return !alreadyExists;
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
	protected abstract String getLastChangedColumnName();

	protected abstract String getLatitudeColumnName();
	protected abstract String getLongitudeColumnName();

	protected abstract long executeInsert(T object, boolean replace);
	protected abstract ContentValues createNonFinalContentValuesFrom(T object);
	protected abstract ContentValues createFinalContentValuesFrom(T object);
	protected abstract T createObjectFrom(Cursor cursor);
}
