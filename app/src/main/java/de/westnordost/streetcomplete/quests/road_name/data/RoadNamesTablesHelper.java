package de.westnordost.streetcomplete.quests.road_name.data;

import android.database.sqlite.SQLiteDatabase;

import de.westnordost.streetcomplete.data.TablesHelper;

public class RoadNamesTablesHelper implements TablesHelper
{
	private static final String ROAD_NAMES =
			"CREATE TABLE " + RoadNamesTable.NAME +
			" (" +
				RoadNamesTable.Columns.WAY_ID +    " int	PRIMARY KEY, " +
				RoadNamesTable.Columns.NAMES +     " blob NOT NULL " +
			");";

	private static final String ROAD_NODE_POSITIONS =
			"CREATE TABLE " + RoadNodePositionsTable.NAME +
			" (" +
				RoadNodePositionsTable.Columns.LATITUDE +   " double  NOT NULL, " +
				RoadNodePositionsTable.Columns.LONGITUDE +  " double  NOT NULL, " +
				RoadNodePositionsTable.Columns.WAY_ID +	    " int     NOT NULL, " +
				"CONSTRAINT primary_key PRIMARY KEY (" +
					RoadNodePositionsTable.Columns.LATITUDE + ", " +
					RoadNodePositionsTable.Columns.LONGITUDE + ", " +
					RoadNodePositionsTable.Columns.WAY_ID +
				") " +
			");";

	private static final String ROAD_NAME_QUEST_SUGGESTIONS =
			"CREATE TABLE " + RoadNamesQuestSuggestionsTable.NAME +
			" (" +
				RoadNamesQuestSuggestionsTable.Columns.QUEST_ID +	" int  NOT NULL, " +
				RoadNamesQuestSuggestionsTable.Columns.WAY_ID +		" int  NOT NULL, " +
				"CONSTRAINT primary_key PRIMARY KEY (" +
					RoadNamesQuestSuggestionsTable.Columns.QUEST_ID + ", " +
					RoadNamesQuestSuggestionsTable.Columns.WAY_ID +
				") " +
			");";

	@Override public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ROAD_NAMES);
		db.execSQL(ROAD_NODE_POSITIONS);
		db.execSQL(ROAD_NAME_QUEST_SUGGESTIONS);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// was introduced in schema version 6
		if(oldVersion < 6 && newVersion >= 6)
		{
			db.execSQL(ROAD_NAMES);
			db.execSQL(ROAD_NODE_POSITIONS);
			db.execSQL(ROAD_NAME_QUEST_SUGGESTIONS);
		}
	}
}
