package de.westnordost.streetcomplete.data.osm.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

public class RelationDaoTest extends ApplicationDbTestCase
{
	private RelationDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dao = new RelationDao(dbHelper, serializer);
	}

	public void testPutGetNoTags()
	{
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0, "outer", Element.Type.WAY));
		members.add(new OsmRelationMember(1, "inner", Element.Type.WAY));
		Relation relation = new OsmRelation(5, 1, members, null);
		dao.put(relation);
		Relation dbRelation = dao.get(5);

		checkEqual(relation, dbRelation);
	}

	public void testPutGetWithTags()
	{
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(0, "outer", Element.Type.WAY));
		members.add(new OsmRelationMember(1, "inner", Element.Type.WAY));
		Map<String,String> tags = new HashMap<>();
		tags.put("a key", "a value");
		Relation relation = new OsmRelation(5, 1, members, tags);
		dao.put(relation);
		Relation dbRelation = dao.get(5);

		checkEqual(relation, dbRelation);
	}

	private void checkEqual(Relation relation, Relation dbRelation)
	{
		assertEquals(relation.getId(), dbRelation.getId());
		assertEquals(relation.getVersion(), dbRelation.getVersion());
		assertEquals(relation.getTags(), dbRelation.getTags());
		assertEquals(relation.getMembers(), dbRelation.getMembers());

	}
}
