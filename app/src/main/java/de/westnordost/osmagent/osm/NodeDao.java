package de.westnordost.osmagent.osm;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.persist.OsmQuestTable;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;

public class NodeDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public NodeDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	/** adds or updates (overwrites) a node */
	public void put(Node node)
	{
		ContentValues values = new ContentValues();
		values.put(NodeTable.Columns.ID, node.getId());
		LatLon pos = node.getPosition();
		values.put(NodeTable.Columns.LATITUDE, pos.getLatitude());
		values.put(NodeTable.Columns.LONGITUDE, pos.getLongitude());
		values.put(NodeTable.Columns.VERSION, node.getVersion());
		if(node.getTags() != null)
		{
			values.put(NodeTable.Columns.TAGS, serializer.toBytes(node.getTags()));
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insertWithOnConflict(NodeTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public Node get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(NodeTable.NAME, null,
				NodeTable.Columns.ID + " = " + id, null, null, null, null, "1");

		if(!cursor.moveToFirst()) return null;

		int colLat = cursor.getColumnIndexOrThrow(NodeTable.Columns.LATITUDE),
			colLon = cursor.getColumnIndexOrThrow(NodeTable.Columns.LONGITUDE),
			colVersion = cursor.getColumnIndexOrThrow(NodeTable.Columns.VERSION),
			colTags = cursor.getColumnIndexOrThrow(NodeTable.Columns.TAGS);

		int version = cursor.getInt(colVersion);
		LatLon latLon = new OsmLatLon(cursor.getDouble(colLat), cursor.getDouble(colLon));
		Map<String,String> tags = null;
		if(!cursor.isNull(colTags))
		{
			serializer.toObject(cursor.getBlob(colTags), Map.class);
		}

		cursor.close();

		return new OsmNode(id, version, latLon, tags, null);
	}

	/** Cleans up element entries that are not referenced by any quest anymore. */
	public void deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where = NodeTable.Columns.ID + " NOT IN ( " +
				"SELECT " + OsmQuestTable.Columns.ELEMENT_ID + " AS " + NodeTable.Columns.ID + " " +
				"FROM " + OsmQuestTable.NAME + " " +
				"WHERE " + OsmQuestTable.Columns.ELEMENT_TYPE + " = " + Node.Type.NODE.ordinal();

		db.delete(NodeTable.NAME, where, null);
	}
}
