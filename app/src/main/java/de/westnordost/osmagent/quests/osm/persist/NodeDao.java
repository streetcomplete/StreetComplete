package de.westnordost.osmagent.quests.osm.persist;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;

public class NodeDao extends AOsmElementDao<Node>
{
	private final Serializer serializer;

	@Inject public NodeDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;
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

	@Override protected ContentValues createContentValuesFrom(Node node)
	{
		ContentValues values = new ContentValues();
		values.put(NodeTable.Columns.ID, node.getId());
		LatLon pos = node.getPosition();
		values.put(NodeTable.Columns.LATITUDE, pos.getLatitude());
		values.put(NodeTable.Columns.LONGITUDE, pos.getLongitude());
		values.put(NodeTable.Columns.VERSION, node.getVersion());
		if(node.getTags() != null)
		{
			HashMap<String,String> map = new HashMap<>();
			map.putAll(node.getTags());
			values.put(NodeTable.Columns.TAGS, serializer.toBytes(map));
		}
		return values;
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
		return new OsmNode(id, version, latLon, tags, null);
	}
}
