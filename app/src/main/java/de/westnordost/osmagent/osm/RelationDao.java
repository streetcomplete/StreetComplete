package de.westnordost.osmagent.osm;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.persist.OsmQuestTable;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

public class RelationDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public RelationDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	/** adds or updates (overwrites) a relation */
	public void put(Relation relation)
	{
		ContentValues values = new ContentValues();
		values.put(RelationTable.Columns.ID, relation.getId());
		values.put(RelationTable.Columns.VERSION, relation.getVersion());
		values.put(RelationTable.Columns.MEMBERS, serializer.toBytes(relation.getMembers()));

		if(relation.getTags() != null)
		{
			values.put(RelationTable.Columns.TAGS, serializer.toBytes(relation.getTags()));
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insertWithOnConflict(RelationTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public Relation get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(RelationTable.NAME, null,
				RelationTable.Columns.ID + " = " + id, null, null, null, null, "1");

		if(!cursor.moveToFirst()) return null;

		int colMembers = cursor.getColumnIndexOrThrow(RelationTable.Columns.MEMBERS),
			colVersion = cursor.getColumnIndexOrThrow(RelationTable.Columns.VERSION),
			colTags = cursor.getColumnIndexOrThrow(RelationTable.Columns.TAGS);

		int version = cursor.getInt(colVersion);
		Map<String,String> tags = null;
		if(!cursor.isNull(colTags))
		{
			tags = serializer.toObject(cursor.getBlob(colTags), Map.class);
		}
		List<RelationMember> members = serializer.toObject(cursor.getBlob(colMembers), List.class);

		cursor.close();

		return new OsmRelation(id, version, members, tags, null);
	}

	/** Cleans up element entries that are not referenced by any quest anymore. */
	public void deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where = RelationTable.Columns.ID + " NOT IN ( " +
				"SELECT " + OsmQuestTable.Columns.ELEMENT_ID + " AS " + RelationTable.Columns.ID + " " +
				"FROM " + OsmQuestTable.NAME + " " +
				"WHERE " + OsmQuestTable.Columns.ELEMENT_TYPE + " = " + Relation.Type.NODE.ordinal();

		db.delete(RelationTable.NAME, where, null);
	}
}
