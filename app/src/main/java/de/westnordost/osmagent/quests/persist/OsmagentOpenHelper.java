package de.westnordost.osmagent.quests.persist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.westnordost.osmagent.osm.NodeTable;
import de.westnordost.osmagent.osm.NoteTable;
import de.westnordost.osmagent.osm.RelationTable;
import de.westnordost.osmagent.osm.WayTable;

public class OsmagentOpenHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "osmagent";
	private static final int DB_VERSION = 1;

	private static final String OSM_QUESTS_TABLE_CREATE =
			"CREATE TABLE " + OsmQuestTable.NAME +
			" (" +
				OsmQuestTable.Columns.QUEST_ID +		" int			PRIMARY KEY, " +
				OsmQuestTable.Columns.QUEST_TYPE +		" varchar(255)	NOT NULL, " +
				OsmQuestTable.Columns.QUEST_STATUS +	" int			NOT NULL" +
				OsmQuestTable.Columns.TAG_CHANGES +		" blob, " + // null if no changes
				OsmQuestTable.Columns.ELEMENT_ID +		" int			NOT NULL, " +
				OsmQuestTable.Columns.ELEMENT_TYPE +	" int			NOT NULL, " +
				"CONSTRAINT same_quest UNIQUE (" +
					OsmQuestTable.Columns.QUEST_TYPE + ", " +
					OsmQuestTable.Columns.ELEMENT_ID + ", " +
					OsmQuestTable.Columns.ELEMENT_TYPE +
				") ON CONFLICT IGNORE, " +
				"CONSTRAINT element_key FOREIGN KEY (" +
					OsmQuestTable.Columns.ELEMENT_TYPE + ", " + OsmQuestTable.Columns.ELEMENT_ID +
				")" +
			");";

	private static final String OSM_NOTES_QUESTS_TABLE_CREATE =
			"CREATE TABLE " + OsmNoteQuestTable.NAME +
			" (" +
				OsmNoteQuestTable.Columns.NOTE_ID +			" int	PRIMARY KEY ON CONFLICT IGNORE, " +
				OsmNoteQuestTable.Columns.QUEST_STATUS +	" int	NOT NULL, " +
				OsmNoteQuestTable.Columns.CHANGES +			" blob" +
			");";

	// TODO create merged view

	private static final String ELEMENTS_GEOMETRY_TABLE_CREATE =
			"CREATE TABLE " + ElementGeometryTable.NAME +
			" (" +
				ElementGeometryTable.Columns.ELEMENT_TYPE +		" int	NOT NULL, " +
				ElementGeometryTable.Columns.ELEMENT_ID +		" int	NOT NULL, " +
				ElementGeometryTable.Columns.ELEMENT_GEOMETRY +	" blob	NOT NULL, " +
				"CONSTRAINT primary_key PRIMARY KEY (" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + ", " +
					ElementGeometryTable.Columns.ELEMENT_ID +
				") " +
			");";

	private static final String QUESTS_VIEW_CREATE =
			"CREATE VIEW " + OsmQuestTable.NAME_MERGED_VIEW + " AS " +
			"SELECT * FROM " + OsmQuestTable.NAME + " " +
				"INNER JOIN " + ElementGeometryTable.NAME + " USING (" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + ", " +
					ElementGeometryTable.Columns.ELEMENT_ID +
				")";

	private static final String NOTES_TABLE_CREATE =
			"CREATE TABLE " + NoteTable.NAME +
					" (" +
					NoteTable.Columns.ID +			" int		PRIMARY KEY, " +
					NoteTable.Columns.LATITUDE + 	" double	NOT NULL," +
					NoteTable.Columns.LONGITUDE + 	" double	NOT NULL," +
					NoteTable.Columns.CREATED +		" int		NOT NULL, " +
					NoteTable.Columns.CLOSED +		" int, " +
					NoteTable.Columns.STATUS + 		" int		NOT NULL," +
					NoteTable.Columns.COMMENTS +	" blob		NOT NULL" +
					");";

	private static final String NODES_TABLE_CREATE =
			"CREATE TABLE " + NodeTable.NAME +
			" (" +
				NodeTable.Columns.ID +			" int		PRIMARY KEY, " +
				NodeTable.Columns.VERSION +		" int		NOT NULL, " +
				NodeTable.Columns.LATITUDE + 	" double	NOT NULL," +
				NodeTable.Columns.LONGITUDE + 	" double	NOT NULL," +
				NodeTable.Columns.TAGS +		" blob" +
			");";

	private static final String WAYS_TABLE_CREATE =
			"CREATE TABLE " + WayTable.NAME +
			" (" +
				WayTable.Columns.ID +		" int	PRIMARY KEY, " +
				WayTable.Columns.VERSION +	" int	NOT NULL, " +
				WayTable.Columns.TAGS +		" blob, " +
				WayTable.Columns.NODE_IDS +	" blob	NOT NULL" +
			");";

	private static final String RELATIONS_TABLE_CREATE =
			"CREATE TABLE " + RelationTable.NAME +
			" (" +
				RelationTable.Columns.ID +		" int	PRIMARY KEY, " +
				RelationTable.Columns.VERSION +	" int	NOT NULL, " +
				RelationTable.Columns.TAGS +	" blob, " +
				RelationTable.Columns.MEMBERS +	" blob NOT NULL" +
			");";

	public OsmagentOpenHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(OSM_QUESTS_TABLE_CREATE);
		db.execSQL(ELEMENTS_GEOMETRY_TABLE_CREATE);
		db.execSQL(NODES_TABLE_CREATE);
		db.execSQL(WAYS_TABLE_CREATE);
		db.execSQL(RELATIONS_TABLE_CREATE);
		db.execSQL(NOTES_TABLE_CREATE);
		db.execSQL(OSM_NOTES_QUESTS_TABLE_CREATE);

		db.execSQL(QUESTS_VIEW_CREATE);
		//db.execSQL(...);
		// TODO
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// for later changes to the DB
	}
}
