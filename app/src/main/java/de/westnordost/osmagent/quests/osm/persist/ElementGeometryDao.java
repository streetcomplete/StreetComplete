package de.westnordost.osmagent.quests.osm.persist;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class ElementGeometryDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public ElementGeometryDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	/** adds or updates (overwrites) an element geometry*/
	public void put(Element.Type type, long id, ElementGeometry geometry)
	{
		ContentValues values = new ContentValues();
		values.put(ElementGeometryTable.Columns.ELEMENT_ID, id);
		values.put(ElementGeometryTable.Columns.ELEMENT_TYPE, type.name());
		values.put(ElementGeometryTable.Columns.GEOMETRY_OUTER, serializer.toBytes(geometry.outer));
		if(geometry.inner != null)
		{
			values.put(ElementGeometryTable.Columns.GEOMETRY_INNER, serializer.toBytes(geometry.inner));
		}
		values.put(ElementGeometryTable.Columns.LATITUDE, geometry.center.getLatitude());
		values.put(ElementGeometryTable.Columns.LONGITUDE, geometry.center.getLongitude());

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insertWithOnConflict(ElementGeometryTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public ElementGeometry get(Element.Type type, long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String where = ElementGeometryTable.Columns.ELEMENT_ID + " = ? AND " +
				ElementGeometryTable.Columns.ELEMENT_TYPE + " = ?";
		String[] args = {String.valueOf(id), type.name()};
		Cursor cursor = db.query(ElementGeometryTable.NAME, null, where, args,
				null, null, null, "1");

		try
		{
			if(!cursor.moveToFirst()) return null;
			return createObjectFrom(serializer, cursor);
		}
		finally
		{
			cursor.close();
		}
	}

	static ElementGeometry createObjectFrom(Serializer serializer, Cursor cursor)
	{
		int colOuterGeometry = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.GEOMETRY_OUTER),
			colInnerGeometry = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.GEOMETRY_INNER),
			colCenterLat = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.LATITUDE),
			colCenterLon = cursor.getColumnIndexOrThrow(ElementGeometryTable.Columns.LONGITUDE);

		ArrayList<ArrayList<LatLon>> outer, inner = null;
		outer = serializer.toObject(cursor.getBlob(colOuterGeometry), ArrayList.class);
		if(!cursor.isNull(colInnerGeometry))
		{
			inner = serializer.toObject(cursor.getBlob(colInnerGeometry), ArrayList.class);
		}
		LatLon center = new OsmLatLon(cursor.getDouble(colCenterLat), cursor.getDouble(colCenterLon));
		return new ElementGeometry(outer, inner, center);
	}

	/** Cleans up element geometry entries that belong to elements that are not referenced by any
	 *  quest anymore. */
	public int deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		/* SQLite does not allow selecting multiple columns in a DELETE subquery. Using a workaround
		*  as described here:
		*  http://blog.programmingsolution.net/sql-server-2008/tsql/delete-rows-of-a-table-matching-multiple-columns-of-another-table/*/
		String lumpTogether = "+'#'+";
		String where =
				"(" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + lumpTogether +
					ElementGeometryTable.Columns.ELEMENT_ID +
				") NOT IN ( SELECT " +
					OsmQuestTable.Columns.ELEMENT_TYPE + lumpTogether +
					OsmQuestTable.Columns.ELEMENT_ID +	" FROM " + OsmQuestTable.NAME + ")";

		return db.delete(ElementGeometryTable.NAME, where, null);
	}
}
