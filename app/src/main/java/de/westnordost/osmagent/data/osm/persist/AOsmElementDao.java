package de.westnordost.osmagent.data.osm.persist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.westnordost.osmapi.map.data.Element;

public abstract class AOsmElementDao<T extends Element>
{
	private final SQLiteOpenHelper dbHelper;

	public AOsmElementDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
	}

	/* Adds or updates the given object to the database */
	public void put(T object)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insertWithOnConflict(getTableName(), null, createContentValuesFrom(object),
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public void delete(long id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(getTableName(), getIdColumnName() + " = " + id, null);
	}

	public T get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(getTableName(), null, getIdColumnName() + " = " + id,
					null, null, null, null, "1");
		try
		{
			if (!cursor.moveToFirst()) return null;
			return createObjectFrom(cursor);
		}
		finally
		{
			cursor.close();
		}
	}

	/** Cleans up element entries that are not referenced by any quest anymore. */
	public void deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where = NodeTable.Columns.ID + " NOT IN ( " +
				"SELECT " + OsmQuestTable.Columns.ELEMENT_ID + " AS " + getIdColumnName() + " " +
				"FROM " + OsmQuestTable.NAME + " " +
				"WHERE " + OsmQuestTable.Columns.ELEMENT_TYPE + " = \"" + getElementTypeName() +"\"" +
				")";

		db.delete(getTableName(), where, null);
	}

	protected abstract String getElementTypeName();

	protected abstract String getTableName();
	protected abstract String getIdColumnName();

	protected abstract ContentValues createContentValuesFrom(T object);
	protected abstract T createObjectFrom(Cursor cursor);
}
