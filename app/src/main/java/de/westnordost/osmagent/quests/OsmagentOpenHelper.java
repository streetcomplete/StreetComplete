package de.westnordost.osmagent.quests;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OsmagentOpenHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "osmagent";
	private static final int DB_VERSION = 1;

	private static final String QUESTS_TABLE_CREATE =
			"CREATE TABLE quests" +
			"(" +
			"questId int NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"questType varchar(255), " +
			"elementId int, " +
			"elementType int, " +
			"elementGeometry blob, " +
			"CONSTRAINT same_quest UNIQUE (questType, elementId, elementType), " +
			"CONSTRAINT element_key FOREIGN KEY (elementId, elementType)" +
			");";

	private static final String NODES_TABLE_CREATE =
			"CREATE TABLE nodes" +
			"(" +
			"nodeId int NOT NULL PRIMARY KEY, " +
			// etc...
			");";

	public OsmagentOpenHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// for later changes to the DB
	}
}
