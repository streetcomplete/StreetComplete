package de.westnordost.streetcomplete.quests.oneway;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.persist.WayTable;

import static android.database.sqlite.SQLiteDatabase.*;

public class WayTrafficFlowDao
{
	protected final SQLiteOpenHelper dbHelper;

	@Inject public WayTrafficFlowDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
	}

	public void put(long wayId, boolean isForward)
	{
		ContentValues values = new ContentValues();
		values.put(WayTrafficFlowTable.Columns.WAY_ID, wayId);
		values.put(WayTrafficFlowTable.Columns.IS_FORWARD, isForward ? 1 : 0);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insertWithOnConflict(WayTrafficFlowTable.NAME, null, values, CONFLICT_REPLACE);
	}

	/** @return whether the direction of road user flow is forward or null if unknown */
	public Boolean isForward(long wayId)
	{
		String[] cols = {WayTrafficFlowTable.Columns.IS_FORWARD};
		String query = WayTrafficFlowTable.Columns.WAY_ID + " = ?";
		String[] args = new String[]{String.valueOf(wayId)};

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try (Cursor cursor = db.query(WayTrafficFlowTable.NAME, cols, query, args, null, null, null, "1"))
		{
			if(cursor.moveToFirst())
			{
				return cursor.getInt(0) != 0;
			}
		}
		return null;
	}

	public void delete(long wayId)
	{
		String query = WayTrafficFlowTable.Columns.WAY_ID + " = ?";
		String[] args = new String[]{String.valueOf(wayId)};

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(WayTrafficFlowTable.NAME, query, args);
	}

	public void deleteUnreferenced()
	{
		String id = WayTrafficFlowTable.Columns.WAY_ID;
		String query = id + " NOT IN (SELECT " + WayTable.Columns.ID + " AS " + id + " FROM osm_ways);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(WayTrafficFlowTable.NAME, query, null);
	}
}
