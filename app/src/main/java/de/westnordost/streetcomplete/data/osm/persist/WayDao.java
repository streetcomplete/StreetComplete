package de.westnordost.streetcomplete.data.osm.persist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Way;

public class WayDao extends AOsmElementDao<Way>
{
	private final Serializer serializer;
	private final SQLiteStatement insert;

	@Inject public WayDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;

		String sql = "INSERT OR REPLACE INTO " + WayTable.NAME + " ("+
				WayTable.Columns.ID+","+
				WayTable.Columns.VERSION+","+
				WayTable.Columns.NODE_IDS+","+
				WayTable.Columns.TAGS+
				") values (?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(sql);
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

	@Override protected void executeInsert(Way way)
	{
		insert.bindLong(1, way.getId());
		insert.bindLong(2, way.getVersion());
		insert.bindBlob(3, serializer.toBytes(new ArrayList<>(way.getNodeIds())));
		if(way.getTags() != null)
		{
			HashMap<String, String> map = new HashMap<>(way.getTags());
			insert.bindBlob(4, serializer.toBytes(map));
		}
		else
		{
			insert.bindNull(4);
		}

		insert.executeInsert();
		insert.clearBindings();
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

		return new OsmWay(id, version, nodeIds, tags);
	}
}
