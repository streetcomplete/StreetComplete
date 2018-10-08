package de.westnordost.streetcomplete.data.tiles;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/** Keeps info in which areas quests have been downloaded already in a tile grid of zoom level 14
 *  (~0.022° per tile -> a few kilometers sidelength)*/
public class DownloadedTilesDao
{
	private final SQLiteOpenHelper dbHelper;

	private final SQLiteStatement insert;

	@Inject
	public DownloadedTilesDao(SQLiteOpenHelper dbHelper)
	{
		this.dbHelper = dbHelper;

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(
				"INSERT OR REPLACE INTO " +
				DownloadedTilesTable.NAME + " ("+
					DownloadedTilesTable.Columns.X+","+
					DownloadedTilesTable.Columns.Y+","+
					DownloadedTilesTable.Columns.QUEST_TYPE+","+
					DownloadedTilesTable.Columns.DATE+
				") values (?,?,?,?);");
	}

	/** Persist that the given quest type has been downloaded in every tile in the given tile range */
	public void put(Rect tiles, String questTypeName)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();
		long time = System.currentTimeMillis();
		for(int x = tiles.left; x <= tiles.right; ++x)
		{
			for(int y = tiles.top; y <= tiles.bottom; ++y)
			{
				insert.bindLong(1,x);
				insert.bindLong(2,y);
				insert.bindString(3, questTypeName);
				insert.bindLong(4,time);
				insert.executeInsert();
				insert.clearBindings();
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	/** Invalidate all quest types within the given tile. (consider them as not-downloaded)*/
	public int remove(Point tile)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String[] whereArgs = {String.valueOf(tile.x), String.valueOf(tile.y)};
		return db.delete(DownloadedTilesTable.NAME,
				DownloadedTilesTable.Columns.X + " = ? AND " +
				DownloadedTilesTable.Columns.Y + " = ?", whereArgs);
	}

	public void removeAll()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + DownloadedTilesTable.NAME);
	}

	/** @return a list of quest type names which have already been downloaded in every tile in the
	 *          given tile range */
	public List<String> get(Rect tiles, long ignoreOlderThan)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		int tileCount = (1 + tiles.width()) * (1 + tiles.height());

		String where =
				DownloadedTilesTable.Columns.X + " BETWEEN ? AND ? AND " +
				DownloadedTilesTable.Columns.Y + " BETWEEN ? AND ? AND " +
				DownloadedTilesTable.Columns.DATE + " > ?";
		String[] whereArgs = {
				String.valueOf(tiles.left), String.valueOf(tiles.right),
				String.valueOf(tiles.top), String.valueOf(tiles.bottom),
				String.valueOf(ignoreOlderThan)
		};

		String[] cols = { DownloadedTilesTable.Columns.QUEST_TYPE};
		String groupBy = DownloadedTilesTable.Columns.QUEST_TYPE;
		String having = "COUNT(*) >= " + tileCount;

		try(Cursor cursor = db.query(DownloadedTilesTable.NAME,	cols, where, whereArgs, groupBy, having, null))
		{
			if(cursor.getCount() == 0) return Collections.emptyList();

			List<String> result = new ArrayList<>(cursor.getCount());

			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					String questTypeName = cursor.getString(0);
					result.add(questTypeName);
					cursor.moveToNext();
				}
			}
			return result;
		}
	}
}
