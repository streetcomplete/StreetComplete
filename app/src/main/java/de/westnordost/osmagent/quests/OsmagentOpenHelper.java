package de.westnordost.osmagent.quests;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Singleton;

import de.westnordost.osmagent.quests.osm.persist.ElementGeometryTable;
import de.westnordost.osmagent.quests.osm.persist.NodeTable;
import de.westnordost.osmagent.quests.osm.persist.OsmQuestTable;
import de.westnordost.osmagent.quests.osmnotes.CreateNoteTable;
import de.westnordost.osmagent.quests.osmnotes.NoteTable;
import de.westnordost.osmagent.quests.osm.persist.RelationTable;
import de.westnordost.osmagent.quests.osm.persist.WayTable;
import de.westnordost.osmagent.quests.osmnotes.OsmNoteQuestTable;
import de.westnordost.osmagent.quests.statistics.QuestStatisticsTable;

@Singleton
public class OsmagentOpenHelper extends SQLiteOpenHelper
{
	public static final String DB_NAME = "osmagent.db";
	public static final int DB_VERSION = 1;

	private static final String OSM_QUESTS_TABLE_CREATE =
			"CREATE TABLE " + OsmQuestTable.NAME +
			" (" +
				OsmQuestTable.Columns.QUEST_ID +		" INTEGER		PRIMARY KEY, " +
				OsmQuestTable.Columns.QUEST_TYPE +		" varchar(255)	NOT NULL, " +
				OsmQuestTable.Columns.QUEST_STATUS +	" varchar(255)	NOT NULL, " +
				OsmQuestTable.Columns.TAG_CHANGES +		" blob, " + // null if no changes
				OsmQuestTable.Columns.LAST_UPDATE + 	" int			NOT NULL, " +
				OsmQuestTable.Columns.ELEMENT_ID +		" int			NOT NULL, " +
				OsmQuestTable.Columns.ELEMENT_TYPE +	" varchar(255)	NOT NULL, " +
				"CONSTRAINT same_osm_quest UNIQUE (" +
					OsmQuestTable.Columns.QUEST_TYPE + ", " +
					OsmQuestTable.Columns.ELEMENT_ID + ", " +
					OsmQuestTable.Columns.ELEMENT_TYPE +
				"), " +
				"CONSTRAINT element_key FOREIGN KEY (" +
					OsmQuestTable.Columns.ELEMENT_TYPE + ", " + OsmQuestTable.Columns.ELEMENT_ID +
				") REFERENCES " + ElementGeometryTable.NAME + " (" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + ", " +
					ElementGeometryTable.Columns.ELEMENT_ID +
				")" +
			");";

	private static final String ELEMENTS_GEOMETRY_TABLE_CREATE =
			"CREATE TABLE " + ElementGeometryTable.NAME +
			" (" +
				ElementGeometryTable.Columns.ELEMENT_TYPE +			" varchar(255)	NOT NULL, " +
				ElementGeometryTable.Columns.ELEMENT_ID +			" int			NOT NULL, " +
				ElementGeometryTable.Columns.GEOMETRY_POLYLINES +	" blob, " +
				ElementGeometryTable.Columns.GEOMETRY_POLYGONS +	" blob, " +
				ElementGeometryTable.Columns.LATITUDE +				" double		NOT NULL, " +
				ElementGeometryTable.Columns.LONGITUDE +			" double		NOT NULL, " +
				"CONSTRAINT primary_key PRIMARY KEY (" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + ", " +
					ElementGeometryTable.Columns.ELEMENT_ID +
				") " +
			");";

	private static final String OSM_QUESTS_VIEW_CREATE =
			"CREATE VIEW " + OsmQuestTable.NAME_MERGED_VIEW + " AS " +
			"SELECT * FROM " + OsmQuestTable.NAME + " " +
				"INNER JOIN " + ElementGeometryTable.NAME + " USING (" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + ", " +
					ElementGeometryTable.Columns.ELEMENT_ID +
				");";

	private static final String OSM_NOTES_QUESTS_TABLE_CREATE =
			"CREATE TABLE " + OsmNoteQuestTable.NAME +
			" (" +
				OsmNoteQuestTable.Columns.QUEST_ID + 		" INTEGER		PRIMARY KEY, " +
				OsmNoteQuestTable.Columns.QUEST_STATUS +	" varchar(255)	NOT NULL, " +
				OsmNoteQuestTable.Columns.COMMENT +			" text, " +
				OsmNoteQuestTable.Columns.LAST_UPDATE + 	" int			NOT NULL, " +
				OsmNoteQuestTable.Columns.NOTE_ID +			" INTEGER		UNIQUE NOT NULL " +
					"REFERENCES " + NoteTable.NAME + "(" + NoteTable.Columns.ID + ")" +
			");";

	private static final String NOTES_TABLE_CREATE =
			"CREATE TABLE " + NoteTable.NAME +
					" (" +
					NoteTable.Columns.ID +			" int			PRIMARY KEY, " +
					NoteTable.Columns.LATITUDE + 	" double		NOT NULL, " +
					NoteTable.Columns.LONGITUDE + 	" double		NOT NULL, " +
					NoteTable.Columns.CREATED +		" int			NOT NULL, " +
					NoteTable.Columns.CLOSED +		" int, " +
					NoteTable.Columns.STATUS + 		" varchar(255)	NOT NULL, " +
					NoteTable.Columns.COMMENTS +	" blob			NOT NULL" +
					");";

	private static final String CREATE_OSM_NOTES_TABLE_CREATE =
			"CREATE TABLE " + CreateNoteTable.NAME +
					" (" +
					CreateNoteTable.Columns.ID + 		" INTEGER		PRIMARY KEY, " +
					CreateNoteTable.Columns.LATITUDE + 	" double		NOT NULL, " +
					CreateNoteTable.Columns.LONGITUDE + " double		NOT NULL, " +
					CreateNoteTable.Columns.TEXT + 		" text			NOT NULL" +
					");";

	private static final String OSM_NOTES_VIEW_CREATE =
			"CREATE VIEW " + OsmNoteQuestTable.NAME_MERGED_VIEW + " AS " +
			"SELECT * FROM " + OsmNoteQuestTable.NAME + " " +
				"INNER JOIN " + NoteTable.NAME + " USING (" + NoteTable.Columns.ID + ");";

	private static final String NODES_TABLE_CREATE =
			"CREATE TABLE " + NodeTable.NAME +
			" (" +
				NodeTable.Columns.ID +			" int		PRIMARY KEY, " +
				NodeTable.Columns.VERSION +		" int		NOT NULL, " +
				NodeTable.Columns.LATITUDE + 	" double	NOT NULL, " +
				NodeTable.Columns.LONGITUDE + 	" double	NOT NULL, " +
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

	private static final String QUEST_STATISTICS_CREATE =
			"CREATE TABLE " + QuestStatisticsTable.NAME +
			" (" +
				QuestStatisticsTable.Columns.QUEST_TYPE +	" varchar(255)	PRIMARY KEY, " +
				QuestStatisticsTable.Columns.SUCCEEDED +	" int			NOT NULL " +
			");";

	public OsmagentOpenHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ELEMENTS_GEOMETRY_TABLE_CREATE);
		db.execSQL(OSM_QUESTS_TABLE_CREATE);

		db.execSQL(NODES_TABLE_CREATE);
		db.execSQL(WAYS_TABLE_CREATE);
		db.execSQL(RELATIONS_TABLE_CREATE);

		db.execSQL(NOTES_TABLE_CREATE);
		db.execSQL(OSM_NOTES_QUESTS_TABLE_CREATE);
		db.execSQL(CREATE_OSM_NOTES_TABLE_CREATE);

		db.execSQL(QUEST_STATISTICS_CREATE);

		db.execSQL(OSM_QUESTS_VIEW_CREATE);
		db.execSQL(OSM_NOTES_VIEW_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// for later changes to the DB
	}
}
