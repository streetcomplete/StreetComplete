package de.westnordost.streetcomplete.quests.oneway;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import static android.database.sqlite.SQLiteDatabase.*;

public class TrafficFlowDao
{
	protected final SQLiteOpenHelper dbHelper;

	@Inject public TrafficFlowDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;
	}

	public void put(long wayId, boolean isForward)
	{
		ContentValues values = new ContentValues();
		values.put(TrafficFlowTable.Columns.WAY_ID, wayId);
		values.put(TrafficFlowTable.Columns.IS_FORWARD, isForward ? 1 : 0);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insertWithOnConflict(TrafficFlowTable.NAME, null, values, CONFLICT_REPLACE);
	}

	/** @return whether the direction of road user flow is forward or null if unknown */
	public Boolean isForward(long wayId)
	{
		String[] cols = {TrafficFlowTable.Columns.IS_FORWARD};
		String query = TrafficFlowTable.Columns.WAY_ID + " = ?";
		String[] args = new String[]{String.valueOf(wayId)};

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try (Cursor cursor = db.query(TrafficFlowTable.NAME, cols, query, args, null, null, null, "1"))
		{
			if(cursor.moveToFirst())
			{
				return cursor.getInt(0) != 0;
			}
		}
		return null;
	}

	public void remove(long wayId)
	{
		String query = TrafficFlowTable.Columns.WAY_ID + " = ?";
		String[] args = new String[]{String.valueOf(wayId)};

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TrafficFlowTable.NAME, query, args);
	}
}
