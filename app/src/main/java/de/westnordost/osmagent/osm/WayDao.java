package de.westnordost.osmagent.osm;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.persist.OsmQuestTable;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Way;

public class WayDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public WayDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	/** adds or updates (overwrites) a way */
	public void put(Way way)
	{
		ContentValues values = new ContentValues();
		values.put(WayTable.Columns.ID, way.getId());
		values.put(WayTable.Columns.VERSION, way.getVersion());
		values.put(WayTable.Columns.NODE_IDS, serializer.toBytes(way.getNodeIds()));

		if(way.getTags() != null)
		{
			values.put(WayTable.Columns.TAGS, serializer.toBytes(way.getTags()));
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insertWithOnConflict(WayTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public Way get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(WayTable.NAME, null,
				WayTable.Columns.ID + " = " + id, null, null, null, null, "1");

		if(!cursor.moveToFirst()) return null;

		int colNodeIds = cursor.getColumnIndexOrThrow(WayTable.Columns.NODE_IDS),
			colVersion = cursor.getColumnIndexOrThrow(WayTable.Columns.VERSION),
			colTags = cursor.getColumnIndexOrThrow(WayTable.Columns.TAGS);

		int version = cursor.getInt(colVersion);
		Map<String, String> tags = null;
		if(!cursor.isNull(colTags))
		{
			tags = serializer.toObject(cursor.getBlob(colTags), Map.class);
		}
		List<Long> nodeIds = serializer.toObject(cursor.getBlob(colNodeIds), List.class);

		cursor.close();

		return new OsmWay(id, version, nodeIds, tags, null);
	}

	/** Cleans up element entries that are not referenced by any quest anymore. */
	public void deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where = WayTable.Columns.ID + " NOT IN ( " +
				"SELECT " + OsmQuestTable.Columns.ELEMENT_ID + " AS " + WayTable.Columns.ID + " " +
				"FROM " + OsmQuestTable.NAME + " " +
				"WHERE " + OsmQuestTable.Columns.ELEMENT_TYPE + " = " + Way.Type.WAY.ordinal();

		db.delete(WayTable.NAME, where, null);
	}
}
