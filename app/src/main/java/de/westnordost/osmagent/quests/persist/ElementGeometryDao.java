package de.westnordost.osmagent.quests.persist;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.ElementGeometry;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.Element;

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
		values.put(ElementGeometryTable.Columns.ELEMENT_TYPE, type.toString());
		values.put(ElementGeometryTable.Columns.ELEMENT_GEOMETRY, serializer.toBytes(geometry));

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insertWithOnConflict(ElementGeometryTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/** Cleans up element geometry entries that belong to elements that are not referenced by any
	 *  quest anymore. */
	public void deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where =
				"(" +
					ElementGeometryTable.Columns.ELEMENT_TYPE + ", " +
					ElementGeometryTable.Columns.ELEMENT_ID +
				") NOT IN ( SELECT " +
					OsmQuestTable.Columns.ELEMENT_TYPE + ", " +
					OsmQuestTable.Columns.ELEMENT_ID +	" FROM " + OsmQuestTable.NAME + ")";

		/* NOT EXISTS ( ... ) String subselect =
				"SELECT * FROM " + OsmQuestTable.NAME + " WHERE " +
				OsmQuestTable.NAME + "." + OsmQuestTable.Columns.ELEMENT_TYPE + " = " +
				ElementGeometryTable.NAME + "." + ElementGeometryTable.Columns.ELEMENT_TYPE +
				" AND " +
				OsmQuestTable.NAME + "." + OsmQuestTable.Columns.ELEMENT_ID + " = " +
				ElementGeometryTable.NAME + "." + ElementGeometryTable.Columns.ELEMENT_ID;*/

		db.delete(ElementGeometryTable.NAME, where, null);
	}
}
