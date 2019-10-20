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
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

public class RelationDao extends AOsmElementDao<Relation>
{
	private final Serializer serializer;
	private final SQLiteStatement insert;

	@Inject public RelationDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;

		String sql = "INSERT OR REPLACE INTO " + RelationTable.NAME + " ("+
				RelationTable.Columns.ID+","+
				RelationTable.Columns.VERSION+","+
				RelationTable.Columns.MEMBERS+","+
				RelationTable.Columns.TAGS+
				") values (?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(sql);
	}

	@Override protected String getTableName()
	{
		return RelationTable.NAME;
	}

	@Override protected String getIdColumnName()
	{
		return RelationTable.Columns.ID;
	}

	@Override protected String getElementTypeName()
	{
		return Relation.Type.RELATION.name();
	}

	@Override protected void executeInsert(Relation relation)
	{
		insert.bindLong(1, relation.getId());
		insert.bindLong(2, relation.getVersion());
		insert.bindBlob(3, serializer.toBytes(new ArrayList<>(relation.getMembers())));
		if(relation.getTags() != null)
		{
			HashMap<String, String> map = new HashMap<>(relation.getTags());
			insert.bindBlob(4, serializer.toBytes(map));
		}
		else
		{
			insert.bindNull(4);
		}

		insert.executeInsert();
		insert.clearBindings();
	}

	@Override protected Relation createObjectFrom(Cursor cursor)
	{
		int colId = cursor.getColumnIndexOrThrow(RelationTable.Columns.ID),
			colMembers = cursor.getColumnIndexOrThrow(RelationTable.Columns.MEMBERS),
			colVersion = cursor.getColumnIndexOrThrow(RelationTable.Columns.VERSION),
			colTags = cursor.getColumnIndexOrThrow(RelationTable.Columns.TAGS);

		long id = cursor.getLong(colId);
		int version = cursor.getInt(colVersion);
		Map<String,String> tags = null;
		if(!cursor.isNull(colTags))
		{
			tags = serializer.toObject(cursor.getBlob(colTags), HashMap.class);
		}
		List<RelationMember> members = serializer.toObject(cursor.getBlob(colMembers), ArrayList.class);

		return new OsmRelation(id, version, members, tags);
	}
}
