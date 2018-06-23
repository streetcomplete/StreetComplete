package de.westnordost.streetcomplete.quests.oneway;

import android.database.sqlite.SQLiteDatabase;

import de.westnordost.streetcomplete.data.TablesHelper;

public class WayTrafficFlowTablesHelper implements TablesHelper
{
	private static final String CREATE_WAY_TRAFFIC_FLOW =
			"CREATE TABLE " + WayTrafficFlowTable.NAME +
			" (" +
				WayTrafficFlowTable.Columns.WAY_ID +     " int PRIMARY KEY, " +
				WayTrafficFlowTable.Columns.IS_FORWARD + " int NOT NULL " +
			");";

	@Override public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_WAY_TRAFFIC_FLOW);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// was introduced in schema version 10
		if(oldVersion < 10 && newVersion >= 10)
		{
			db.execSQL(CREATE_WAY_TRAFFIC_FLOW);
		}
		// all data was invalidated on version 11
		if(oldVersion < 11 && newVersion >= 11)
		{
			db.delete(WayTrafficFlowTable.NAME, null, null);
		}
	}
}
