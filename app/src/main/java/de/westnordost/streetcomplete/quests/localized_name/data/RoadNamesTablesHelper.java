package de.westnordost.streetcomplete.quests.localized_name.data;

import android.database.sqlite.SQLiteDatabase;

import de.westnordost.streetcomplete.data.TablesHelper;

public class RoadNamesTablesHelper implements TablesHelper
{
	private static final String ROAD_NAMES =
			"CREATE TABLE " + RoadNamesTable.NAME +
			" (" +
				RoadNamesTable.Columns.WAY_ID +        " int  PRIMARY KEY, " +
				RoadNamesTable.Columns.NAMES +         " blob NOT NULL, " +
				RoadNamesTable.Columns.GEOMETRY +      " blob NOT NULL, " +
				RoadNamesTable.Columns.MIN_LATITUDE +  " double NOT NULL, " +
				RoadNamesTable.Columns.MIN_LONGITUDE + " double NOT NULL, " +
				RoadNamesTable.Columns.MAX_LATITUDE +  " double NOT NULL, " +
				RoadNamesTable.Columns.MAX_LONGITUDE + " double NOT NULL " +
			");";

	@Override public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ROAD_NAMES);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// was introduced in schema version 6
		if(oldVersion < 6 && newVersion >= 6)
		{
			db.execSQL(ROAD_NAMES);
		}
	}
}
