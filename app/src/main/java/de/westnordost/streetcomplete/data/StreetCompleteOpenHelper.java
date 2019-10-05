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
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable;
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable;
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
	public static final int DB_VERSION = 12;

	private static final String QUEST_STATISTICS_TABLE_CREATE =
			"CREATE TABLE " + QuestStatisticsTable.NAME +
			" (" +
				QuestStatisticsTable.Columns.QUEST_TYPE +	" varchar(255)	PRIMARY KEY, " +
				QuestStatisticsTable.Columns.SUCCEEDED +	" int			NOT NULL " +
			");";

	private final TablesHelper[] extensions;

	public StreetCompleteOpenHelper(Context context, String dbName, TablesHelper[] extensions)
	{
		super(context, dbName, null, DB_VERSION);
		this.extensions = extensions;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ElementGeometryTable.CREATE);
		db.execSQL(OsmQuestTable.CREATE);
		db.execSQL(UndoOsmQuestTable.CREATE);

		db.execSQL(NodeTable.CREATE);
		db.execSQL(WayTable.CREATE);
		db.execSQL(RelationTable.CREATE);

		db.execSQL(NoteTable.CREATE);
		db.execSQL(OsmNoteQuestTable.CREATE);
		db.execSQL(CreateNoteTable.CREATE);

		db.execSQL(QUEST_STATISTICS_TABLE_CREATE);

		db.execSQL(DownloadedTilesTable.CREATE);

		db.execSQL(OsmQuestTable.CREATE_VIEW);
		db.execSQL(UndoOsmQuestTable.MERGED_VIEW_CREATE);
		db.execSQL(OsmNoteQuestTable.CREATE_VIEW);

		db.execSQL(OpenChangesetsTable.CREATE);

		db.execSQL(QuestVisibilityTable.CREATE);

		db.execSQL(OsmQuestSplitWayTable.CREATE);

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
			db.execSQL(OsmQuestTable.CREATE_DB_VERSION_3);
			String allColumns = TextUtils.join(",", OsmQuestTable.INSTANCE.getALL_COLUMNS_DB_VERSION_3());
			db.execSQL("INSERT INTO " + tableName + "(" + allColumns + ") " +
					   " SELECT " + allColumns + " FROM " + oldTableName);
			db.execSQL("DROP TABLE " + oldTableName);
		}

		if(oldVersion < 3 && newVersion >= 3)
		{
			db.execSQL(OpenChangesetsTable.CREATE);
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
			db.execSQL(OpenChangesetsTable.CREATE);
		}

		if(oldVersion < 5 && newVersion >= 5)
		{
			db.execSQL("ALTER TABLE " + CreateNoteTable.NAME + " ADD COLUMN " +
				CreateNoteTable.Columns.QUEST_TITLE + " text;");
		}

		if(oldVersion < 7 && newVersion >= 7)
		{
			db.execSQL(UndoOsmQuestTable.CREATE);
			db.execSQL(UndoOsmQuestTable.MERGED_VIEW_CREATE);
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
			db.execSQL(QuestVisibilityTable.CREATE);
		}

		// all oneway quest data was invalidated on version 11
		if(oldVersion < 11 && newVersion >= 11)
		{
			String where = OsmQuestTable.Columns.QUEST_TYPE + " = ?";
			String[] args = {AddOneway.class.getSimpleName()};
			db.delete(OsmQuestTable.NAME, where, args);
			db.delete(UndoOsmQuestTable.NAME, where, args);
		}

		if(oldVersion < 12 && newVersion >= 12)
		{
			db.execSQL(OsmQuestSplitWayTable.CREATE);
			// slightly different structure for undo osm quest table. Isn't worth converting
			db.execSQL("DROP TABLE " + UndoOsmQuestTable.NAME);
			db.execSQL("DROP VIEW " + UndoOsmQuestTable.NAME_MERGED_VIEW);
			db.execSQL(UndoOsmQuestTable.CREATE);
			db.execSQL(UndoOsmQuestTable.MERGED_VIEW_CREATE);
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
			while(cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
				if (columnName.equals(name)) return true;
			}
		}
		return false;
	}
}
