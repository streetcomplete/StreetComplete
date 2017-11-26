package de.westnordost.streetcomplete.data.osm.persist;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class ElementGeometryDao
{
	private final SQLiteOpenHelper dbHelper;
	private final Serializer serializer;

	private final SQLiteStatement insert;

	public static class Row
	{
		public Row(Element.Type elementType, long elementId, ElementGeometry geometry)
		{
			this.elementType = elementType;
			this.elementId = elementId;
			this.geometry = geometry;
		}

		public Element.Type elementType;
		public long elementId;
		public ElementGeometry geometry;
	}

	@Inject public ElementGeometryDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;

		String sql = "INSERT OR REPLACE INTO " + ElementGeometryTable.NAME + " ("+
				ElementGeometryTable.Columns.ELEMENT_TYPE+","+
				ElementGeometryTable.Columns.ELEMENT_ID+","+
				ElementGeometryTable.Columns.GEOMETRY_POLYGONS+","+
				ElementGeometryTable.Columns.GEOMETRY_POLYLINES+","+
				ElementGeometryTable.Columns.LATITUDE+","+
				ElementGeometryTable.Columns.LONGITUDE+
				") values (?,?,?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(sql);
	}

	public void putAll(Collection<Row> rows)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();

		for(Row row : rows)
		{
			executeInsert(row.elementType, row.elementId, row.geometry);
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	/** adds or updates (overwrites) an element geometry*/
	public void put(Element.Type type, long id, ElementGeometry geometry)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		executeInsert(type, id, geometry);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private void executeInsert(Element.Type type, long id, ElementGeometry geometry)
	{
		insert.bindString(1, type.name());
		insert.bindLong(2, id);
		if (geometry.polygons != null)
			insert.bindBlob(3, serializer.toBytes(geometry.polygons));
		else
			insert.bindNull(3);
		if (geometry.polylines != null)
			insert.bindBlob(4, serializer.toBytes(geometry.polylines));
		else
			insert.bindNull(4);
		insert.bindDouble(5, geometry.center.getLatitude());
		insert.bindDouble(6, geometry.center.getLongitude());

		insert.executeInsert();
		insert.clearBindings();
	}

	public ElementGeometry get(Element.Type type, long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String where = ElementGeometryTable.Columns.ELEMENT_TYPE + " = ? AND " +
				ElementGeometryTable.Columns.ELEMENT_ID + " = ?";
		String[] args = {type.name(), String.valueOf(id)};

		try (Cursor cursor = db.query(ElementGeometryTable.NAME, null, where, args,
				null, null, null, "1"))
		{
			if (!cursor.moveToFirst()) return null;
			return createObjectFrom(serializer, cursor);
		}
	}

	static ElementGeometry createObjectFrom(Serializer serializer, Cursor cursor)
	{
		int colGeometryPolygons = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.GEOMETRY_POLYGONS),
			colGeometryPolylines = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.GEOMETRY_POLYLINES),
			colCenterLat = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.LATITUDE),
			colCenterLon = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.LONGITUDE);

		List<List<LatLon>> polygons = null, polylines = null;

		if(!cursor.isNull(colGeometryPolygons))
		{
			polygons = serializer.toObject(cursor.getBlob(colGeometryPolygons), ArrayList.class);
		}
		if(!cursor.isNull(colGeometryPolylines))
		{
			polylines = serializer.toObject(cursor.getBlob(colGeometryPolylines), ArrayList.class);
		}
		LatLon center = new OsmLatLon(cursor.getDouble(colCenterLat), cursor.getDouble(colCenterLon));
		return new ElementGeometry(polylines, polygons, center);
	}

	/** Cleans up element geometry entries that belong to elements that are not referenced by any
	 *  quest anymore. */
	public int deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		/* SQLite does not allow selecting multiple columns in a DELETE subquery. Using a workaround
		*  as described here:
		*  http://blog.programmingsolution.net/sql-server-2008/tsql/delete-rows-of-a-table-matching-multiple-columns-of-another-table/*/
		String where =
				"(" +
					ElementGeometryTable.Columns.ELEMENT_TYPE +
					LUMP +
					ElementGeometryTable.Columns.ELEMENT_ID +
				")  NOT IN ( " +
					getSelectAllElementsIn(OsmQuestTable.NAME) +
					" UNION " +
					getSelectAllElementsIn(OsmQuestTable.NAME_UNDO) +
				")";

		return db.delete(ElementGeometryTable.NAME, where, null);
	}

	private static final String LUMP = "+'#'+";

	private static String getSelectAllElementsIn(String table)
	{
		return "SELECT " + OsmQuestTable.Columns.ELEMENT_TYPE + LUMP + OsmQuestTable.Columns.ELEMENT_ID +
				" FROM " + table;
	}
}
