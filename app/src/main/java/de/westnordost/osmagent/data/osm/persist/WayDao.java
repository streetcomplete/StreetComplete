package de.westnordost.osmagent.data.osm.persist;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Way;

public class WayDao extends AOsmElementDao<Way>
{
	private final Serializer serializer;

	@Inject public WayDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;
	}

	@Override protected String getTableName()
	{
		return WayTable.NAME;
	}

	@Override protected String getIdColumnName()
	{
		return WayTable.Columns.ID;
	}

	@Override protected String getElementTypeName()
	{
		return Way.Type.WAY.name();
	}

	@Override protected ContentValues createContentValuesFrom(Way way)
	{
		ContentValues values = new ContentValues();
		values.put(WayTable.Columns.ID, way.getId());
		values.put(WayTable.Columns.VERSION, way.getVersion());

		values.put(WayTable.Columns.NODE_IDS, serializer.toBytes(new ArrayList<>(way.getNodeIds())));

		if(way.getTags() != null)
		{
			HashMap<String,String> map = new HashMap<>();
			map.putAll(way.getTags());
			values.put(WayTable.Columns.TAGS, serializer.toBytes(map));
		}

		return values;
	}

	@Override protected Way createObjectFrom(Cursor cursor)
	{
		int colId = cursor.getColumnIndexOrThrow(WayTable.Columns.ID),
			colNodeIds = cursor.getColumnIndexOrThrow(WayTable.Columns.NODE_IDS),
			colVersion = cursor.getColumnIndexOrThrow(WayTable.Columns.VERSION),
			colTags = cursor.getColumnIndexOrThrow(WayTable.Columns.TAGS);

		long id = cursor.getLong(colId);
		int version = cursor.getInt(colVersion);
		Map<String, String> tags = null;
		if(!cursor.isNull(colTags))
		{
			tags = serializer.toObject(cursor.getBlob(colTags), HashMap.class);
		}
		List<Long> nodeIds = serializer.toObject(cursor.getBlob(colNodeIds), ArrayList.class);

		return new OsmWay(id, version, nodeIds, tags, null);
	}
}
