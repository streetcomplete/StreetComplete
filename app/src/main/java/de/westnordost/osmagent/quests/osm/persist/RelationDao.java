package de.westnordost.osmagent.quests.osm.persist;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

public class RelationDao extends AOsmElementDao<Relation>
{
	private final Serializer serializer;

	@Inject public RelationDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;
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

	@Override protected ContentValues createContentValuesFrom(Relation relation)
	{
		ContentValues values = new ContentValues();
		values.put(RelationTable.Columns.ID, relation.getId());
		values.put(RelationTable.Columns.VERSION, relation.getVersion());
		values.put(RelationTable.Columns.MEMBERS,
				serializer.toBytes(new ArrayList<>(relation.getMembers())));

		if(relation.getTags() != null)
		{
			HashMap<String,String> map = new HashMap<>();
			map.putAll(relation.getTags());
			values.put(RelationTable.Columns.TAGS, serializer.toBytes(map));
		}
		return values;
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

		return new OsmRelation(id, version, members, tags, null);
	}
}
