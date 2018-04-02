package de.westnordost.streetcomplete.quests.oneway;

import android.database.sqlite.SQLiteDatabase;

import de.westnordost.streetcomplete.data.TablesHelper;

public class TrafficFlowTablesHelper implements TablesHelper
{
	private static final String CREATE_TRAFFIC_FLOW =
			"CREATE TABLE " + TrafficFlowTable.NAME +
			" (" +
				TrafficFlowTable.Columns.WAY_ID +     " int PRIMARY KEY, " +
				TrafficFlowTable.Columns.IS_FORWARD + " int NOT NULL " +
			");";

	@Override public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_TRAFFIC_FLOW);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// was introduced in schema version 10
		if(oldVersion < 10 && newVersion >= 10)
		{
			db.execSQL(CREATE_TRAFFIC_FLOW);
		}
	}
}
