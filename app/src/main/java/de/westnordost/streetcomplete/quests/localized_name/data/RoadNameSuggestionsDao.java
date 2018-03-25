package de.westnordost.streetcomplete.quests.localized_name.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class RoadNameSuggestionsDao
{
	protected final SQLiteOpenHelper dbHelper;

	private final Serializer serializer;
	private final SQLiteStatement insert;

	@Inject public RoadNameSuggestionsDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(
				"INSERT OR REPLACE INTO " + RoadNamesTable.NAME + " ("+
						RoadNamesTable.Columns.WAY_ID + "," +
						RoadNamesTable.Columns.NAMES + "," +
						RoadNamesTable.Columns.GEOMETRY + "," +
						RoadNamesTable.Columns.MIN_LATITUDE + "," +
						RoadNamesTable.Columns.MIN_LONGITUDE + "," +
						RoadNamesTable.Columns.MAX_LATITUDE + "," +
						RoadNamesTable.Columns.MAX_LONGITUDE +
				") values (?,?,?,?,?,?,?);");
	}

	public void putRoad(long wayId, HashMap<String,String> namesByLanguage, ArrayList<LatLon> geometry)
	{
		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(geometry);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();

		insert.bindLong(1, wayId);
		insert.bindBlob(2, serializer.toBytes(namesByLanguage));
		insert.bindBlob(3, serializer.toBytes(geometry));
		insert.bindDouble(4, bbox.getMinLatitude());
		insert.bindDouble(5, bbox.getMinLongitude());
		insert.bindDouble(6, bbox.getMaxLatitude());
		insert.bindDouble(7, bbox.getMaxLongitude());

		insert.executeInsert();
		insert.clearBindings();

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public List<Map<String,String>> getNames(List<LatLon> points, double maxDistance)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		// preselection via intersection check of bounding boxes

		String rangeQuery =
				RoadNamesTable.Columns.MIN_LATITUDE +  " <= ? AND " +
				RoadNamesTable.Columns.MIN_LONGITUDE + " <= ? AND " +
				RoadNamesTable.Columns.MAX_LATITUDE +  " >= ? AND " +
				RoadNamesTable.Columns.MAX_LONGITUDE + " >= ? ";

		StringBuilder query = new StringBuilder();
		String[] args = new String[points.size() * 4];
		for (int i = 0; i < points.size(); i++)
		{
			if(i != 0) query.append(" OR ");
			query.append(rangeQuery);
			LatLon point = points.get(i);
			BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(point, maxDistance);
			int ai = i*4;
			args[ai + 0] = "" + bbox.getMaxLatitude();
			args[ai + 1] = "" + bbox.getMaxLongitude();
			args[ai + 2] = "" + bbox.getMinLatitude();
			args[ai + 3] = "" + bbox.getMinLongitude();
		}

		List<Map<String,String>> result = new ArrayList<>();

		String[] cols = new String[]{RoadNamesTable.Columns.GEOMETRY, RoadNamesTable.Columns.NAMES};

		try (Cursor cursor = db.query(RoadNamesTable.NAME, cols, query.toString(), args, null, null, null))
		{
			if (cursor.moveToFirst())
			{
				while (!cursor.isAfterLast())
				{
					ArrayList<LatLon> geometry = serializer.toObject(cursor.getBlob(0), ArrayList.class);
					if (SphericalEarthMath.isWithinDistance(maxDistance, points, geometry))
					{
						result.add(serializer.toObject(cursor.getBlob(1), HashMap.class));
					}
					cursor.moveToNext();
				}
			}
		}
		return result;
	}
}
