package de.westnordost.streetcomplete.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import javax.inject.Singleton;

import de.westnordost.streetcomplete.data.changesets.OpenChangesetsTable;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable;
import de.westnordost.streetcomplete.data.osm.persist.NodeTable;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable;
import de.westnordost.streetcomplete.data.osmnotes.NoteTable;
import de.westnordost.streetcomplete.data.osm.persist.RelationTable;
import de.westnordost.streetcomplete.data.osm.persist.WayTable;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestTable;
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsTable;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesTable;
import de.westnordost.streetcomplete.quests.oneway.AddOneway;

@Singleton
public class StreetCompleteOpenHelper extends SQLiteOpenHelper
{
	public static final String DB_NAME = "streetcomplete.db";
	public static final int DB_VERSION = 11;

	private static final String OSM_QUESTS_CREATE_PARAMS = " (" +
			OsmQuestTable.Columns.QUEST_ID +		" INTEGER		PRIMARY KEY, " +
			OsmQuestTable.Columns.QUEST_TYPE +		" varchar(255)	NOT NULL, " +
			OsmQuestTable.Columns.QUEST_STATUS +	" varchar(255)	NOT NULL, " +
			OsmQuestTable.Columns.TAG_CHANGES +		" blob, " + // null if no changes
			OsmQuestTable.Columns.CHANGES_SOURCE +	" varchar(255), " +
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

	private static final String OSM_QUESTS_TABLE_CREATE_DB_VERSION_3 =
			"CREATE TABLE " + OsmQuestTable.NAME + " (" +
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


	private static final String OSM_QUESTS_TABLE_CREATE =
			"CREATE TABLE " + OsmQuestTable.NAME + OSM_QUESTS_CREATE_PARAMS;

	private static final String UNDO_OSM_QUESTS_TABLE_CREATE =
			"CREATE TABLE " + OsmQuestTable.NAME_UNDO + OSM_QUESTS_CREATE_PARAMS;

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

	private static final String OSM_UNDO_QUESTS_VIEW_CREATE =
			"CREATE VIEW " + OsmQuestTable.NAME_UNDO_MERGED_VIEW + " AS " +
					"SELECT * FROM " + OsmQuestTable.NAME_UNDO + " " +
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
				OsmNoteQuestTable.Columns.NOTE_ID +			" INTEGER		UNIQUE NOT NULL, " +
				OsmNoteQuestTable.Columns.IMAGE_PATHS +		" blob " +
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
					CreateNoteTable.Columns.ELEMENT_TYPE +	" varchar(255), " +
					CreateNoteTable.Columns.ELEMENT_ID +	" int, " +
					CreateNoteTable.Columns.TEXT + 		" text			NOT NULL, " +
					CreateNoteTable.Columns.QUEST_TITLE + " text, " +
					CreateNoteTable.Columns.IMAGE_PATHS + " blob" +
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

	private static final String QUEST_STATISTICS_TABLE_CREATE =
			"CREATE TABLE " + QuestStatisticsTable.NAME +
			" (" +
				QuestStatisticsTable.Columns.QUEST_TYPE +	" varchar(255)	PRIMARY KEY, " +
				QuestStatisticsTable.Columns.SUCCEEDED +	" int			NOT NULL " +
			");";

	private static final String DOWNLOADED_TILES_TABLE_CREATE =
			"CREATE TABLE " + DownloadedTilesTable.NAME +
			" (" +
				DownloadedTilesTable.Columns.X +			" int	NOT NULL, " +
				DownloadedTilesTable.Columns.Y +			" int	NOT NULL, " +
				DownloadedTilesTable.Columns.QUEST_TYPE + 	" varchar(255) NOT NULL, " +
				DownloadedTilesTable.Columns.DATE +			" int	NOT NULL, " +
				"CONSTRAINT primary_key PRIMARY KEY (" +
					DownloadedTilesTable.Columns.X + ", " +
					DownloadedTilesTable.Columns.Y + ", " +
					DownloadedTilesTable.Columns.QUEST_TYPE +
				") " +
			");";

	private static final String OPEN_CHANGESETS_TABLE_CREATE =
			"CREATE TABLE " + OpenChangesetsTable.NAME +
			" (" +
				OpenChangesetsTable.Columns.QUEST_TYPE +    " varchar(255), " +
				OpenChangesetsTable.Columns.SOURCE +		" varchar(255), " +
				OpenChangesetsTable.Columns.CHANGESET_ID +  " int	NOT NULL, " +
				"CONSTRAINT primary_key PRIMARY KEY (" +
					OpenChangesetsTable.Columns.QUEST_TYPE + ", " +
					OpenChangesetsTable.Columns.SOURCE +
				") " +
			");";

	private static final String QUEST_VISIBILITY_TABLE_CREATE =
			"CREATE TABLE " + QuestVisibilityTable.NAME +
			" (" +
				QuestVisibilityTable.Columns.QUEST_TYPE +    " varchar(255) PRIMARY KEY, " +
				QuestVisibilityTable.Columns.VISIBILITY +    " int NOT NULL " +
			");";

	private final TablesHelper[] extensions;

	public StreetCompleteOpenHelper(Context context, TablesHelper[] extensions)
	{
		super(context, DB_NAME, null, DB_VERSION);
		this.extensions = extensions;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ELEMENTS_GEOMETRY_TABLE_CREATE);
		db.execSQL(OSM_QUESTS_TABLE_CREATE);
		db.execSQL(UNDO_OSM_QUESTS_TABLE_CREATE);

		db.execSQL(NODES_TABLE_CREATE);
		db.execSQL(WAYS_TABLE_CREATE);
		db.execSQL(RELATIONS_TABLE_CREATE);

		db.execSQL(NOTES_TABLE_CREATE);
		db.execSQL(OSM_NOTES_QUESTS_TABLE_CREATE);
		db.execSQL(CREATE_OSM_NOTES_TABLE_CREATE);

		db.execSQL(QUEST_STATISTICS_TABLE_CREATE);

		db.execSQL(DOWNLOADED_TILES_TABLE_CREATE);

		db.execSQL(OSM_QUESTS_VIEW_CREATE);
		db.execSQL(OSM_UNDO_QUESTS_VIEW_CREATE);
		db.execSQL(OSM_NOTES_VIEW_CREATE);

		db.execSQL(OPEN_CHANGESETS_TABLE_CREATE);

		db.execSQL(QUEST_VISIBILITY_TABLE_CREATE);

		for (TablesHelper extension : extensions)
		{
			extension.onCreate(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// in version 2, the commit_message field was added, in version 3, removed again.
		// Unfortunately, dropping a column in SQLite is not possible using ALTER TABLE ... DROP ...
		// so we copy the whole content of the table into a new table
		if(oldVersion == 2)
		{
			String tableName = OsmQuestTable.NAME;
			String oldTableName = tableName + "_old";
			db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + oldTableName );
			db.execSQL(OSM_QUESTS_TABLE_CREATE_DB_VERSION_3);
			String allColumns = TextUtils.join(",", OsmQuestTable.Columns.ALL_DB_VERSION_3);
			db.execSQL("INSERT INTO " + tableName + "(" + allColumns + ") " +
					   " SELECT " + allColumns + " FROM " + oldTableName);
			db.execSQL("DROP TABLE " + oldTableName);
		}

		if(oldVersion < 3 && newVersion >= 3)
		{
			db.execSQL(OPEN_CHANGESETS_TABLE_CREATE);
		}

		if(oldVersion < 4 && newVersion >= 4)
		{
			if(!tableHasColumn(db, OsmQuestTable.NAME, OsmQuestTable.Columns.CHANGES_SOURCE))
			{
				db.execSQL("ALTER TABLE " + OsmQuestTable.NAME + " ADD COLUMN " +
						OsmQuestTable.Columns.CHANGES_SOURCE + " varchar(255);");
			}
			db.execSQL("UPDATE " + OsmQuestTable.NAME + " SET " +
					OsmQuestTable.Columns.CHANGES_SOURCE + " = 'survey' WHERE " +
					OsmQuestTable.Columns.CHANGES_SOURCE + " ISNULL;");

			// sqlite does not support dropping/altering constraints. Need to create new table.
			// For simplicity sake, we just drop the old table and create it anew, this has the
			// effect that all currently open changesets will not be used but instead new ones are
			// created. That's okay because OSM server closes open changesets after 1h automatically.
			db.execSQL("DROP TABLE " + OpenChangesetsTable.NAME + ";");
			db.execSQL(OPEN_CHANGESETS_TABLE_CREATE);
		}

		if(oldVersion < 5 && newVersion >= 5)
		{
			db.execSQL("ALTER TABLE " + CreateNoteTable.NAME + " ADD COLUMN " +
				CreateNoteTable.Columns.QUEST_TITLE + " text;");
		}

		if(oldVersion < 7 && newVersion >= 7)
		{
			db.execSQL(UNDO_OSM_QUESTS_TABLE_CREATE);
			db.execSQL(OSM_UNDO_QUESTS_VIEW_CREATE);
		}

		if(oldVersion < 8 && newVersion >= 8)
		{
			db.execSQL("ALTER TABLE " + CreateNoteTable.NAME + " ADD COLUMN " +
					CreateNoteTable.Columns.IMAGE_PATHS + " blob ;");
			db.execSQL("ALTER TABLE " + OsmNoteQuestTable.NAME + " ADD COLUMN " +
					OsmNoteQuestTable.Columns.IMAGE_PATHS + " blob ;");
		}

		if(oldVersion < 9 && newVersion >= 9)
		{
			db.execSQL(QUEST_VISIBILITY_TABLE_CREATE);
		}

		// all oneway quest data was invalidated on version 11
		if(oldVersion < 11 && newVersion >= 11)
		{
			String where = OsmQuestTable.Columns.QUEST_TYPE + " = ?";
			String[] args = {AddOneway.class.getSimpleName()};
			db.delete(OsmQuestTable.NAME, where, args);
			db.delete(OsmQuestTable.NAME_UNDO, where, args);
		}

		// for later changes to the DB
		// ...

		for (TablesHelper extension : extensions)
		{
			extension.onUpgrade(db, oldVersion, newVersion);
		}
	}


	private static boolean tableHasColumn(SQLiteDatabase db, String tableName, String columnName)
	{

		try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null))
		{
			if (cursor.moveToFirst())
			{
				while (!cursor.isAfterLast())
				{
					String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
					if (columnName.equals(name)) return true;
					cursor.moveToNext();
				}
			}
		}
		return false;
	}
}
