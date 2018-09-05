package de.westnordost.streetcomplete.data.osm.persist;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;

public class NodeDao extends AOsmElementDao<Node>
{
	private final Serializer serializer;

	private final SQLiteStatement insert;

	@Inject public NodeDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;

		String sql = "INSERT OR REPLACE INTO " + NodeTable.NAME + " ("+
				NodeTable.Columns.ID+","+
				NodeTable.Columns.VERSION+","+
				NodeTable.Columns.LATITUDE+","+
				NodeTable.Columns.LONGITUDE+","+
				NodeTable.Columns.TAGS+
				") values (?,?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(sql);
	}

	@Override protected String getTableName()
	{
		return NodeTable.NAME;
	}

	@Override protected String getIdColumnName()
	{
		return NodeTable.Columns.ID;
	}

	@Override protected String getElementTypeName()
	{
		return Node.Type.NODE.name();
	}


	@Override protected void executeInsert(Node node)
	{
		insert.bindLong(1, node.getId());
		insert.bindLong(2, node.getVersion());
		insert.bindDouble(3, node.getPosition().getLatitude());
		insert.bindDouble(4, node.getPosition().getLongitude());
		if(node.getTags() != null)
		{
			HashMap<String, String> map = new HashMap<>(node.getTags());
			insert.bindBlob(5, serializer.toBytes(map));
		}
		else
		{
			insert.bindNull(5);
		}

		insert.executeInsert();
		insert.clearBindings();
	}

	@Override protected Node createObjectFrom(Cursor cursor)
	{
		int colId = cursor.getColumnIndexOrThrow(NodeTable.Columns.ID),
			colLat = cursor.getColumnIndexOrThrow(NodeTable.Columns.LATITUDE),
			colLon = cursor.getColumnIndexOrThrow(NodeTable.Columns.LONGITUDE),
			colVersion = cursor.getColumnIndexOrThrow(NodeTable.Columns.VERSION),
			colTags = cursor.getColumnIndexOrThrow(NodeTable.Columns.TAGS);

		long id = cursor.getLong(colId);
		int version = cursor.getInt(colVersion);
		LatLon latLon = new OsmLatLon(cursor.getDouble(colLat), cursor.getDouble(colLon));
		Map<String,String> tags = null;
		if(!cursor.isNull(colTags))
		{
			tags = serializer.toObject(cursor.getBlob(colTags), HashMap.class);
		}
		return new OsmNode(id, version, latLon, tags);
	}
}
