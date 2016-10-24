package de.westnordost.osmagent.data.osm.persist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmagent.data.OsmagentDbTestCase;
import de.westnordost.osmagent.data.QuestStatus;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osm.changes.StringMapChanges;
import de.westnordost.osmagent.data.osm.changes.StringMapEntryAdd;
import de.westnordost.osmagent.data.osm.changes.StringMapEntryChange;
import de.westnordost.osmagent.data.osm.changes.StringMapEntryDelete;
import de.westnordost.osmagent.data.osm.changes.StringMapEntryModify;
import de.westnordost.osmagent.data.osm.persist.test.TestQuestType;
import de.westnordost.osmagent.data.osm.persist.test.TestQuestType2;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class OsmQuestDaoTest extends OsmagentDbTestCase
{
	private ElementGeometryDao geometryDao;
	private OsmQuestDao dao;

	@Override public void setUp()
	{
		super.setUp();
		geometryDao = new ElementGeometryDao(dbHelper, serializer);
		dao = new OsmQuestDao(dbHelper, serializer,
				"de.westnordost.osmagent.data.osm.persist.test");
	}

	public void testAddGetNoChanges()
	{
		ElementGeometry geometry = new ElementGeometry(new OsmLatLon(5,5));
		OsmQuest quest = new OsmQuest(null, new TestQuestType(), Element.Type.NODE, 11,
				QuestStatus.ANSWERED, null, new Date(1000), geometry);

		geometryDao.put(quest.getElementType(), quest.getElementId(), geometry);

		dao.add(quest);
		assertEquals(1, (long) quest.getId());
		OsmQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	public void testAddGetWithChanges()
	{
		ElementGeometry geometry = new ElementGeometry(new OsmLatLon(5,5));
		List<StringMapEntryChange> changes = new ArrayList<>();
		changes.add(new StringMapEntryAdd("a key", "a value"));
		changes.add(new StringMapEntryDelete("delete this","key"));
		changes.add(new StringMapEntryModify("modify","this","to that"));
		OsmQuest quest = new OsmQuest(
				null, new TestQuestType(), Element.Type.NODE, 11, QuestStatus.ANSWERED,
				new StringMapChanges(changes), new Date(1000), geometry);

		geometryDao.put(quest.getElementType(), quest.getElementId(), geometry);

		dao.add(quest);
		assertEquals(1, (long) quest.getId());
		OsmQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	public void testGetAllByBBoxAndType()
	{
		ElementGeometry geometry = new ElementGeometry(new OsmLatLon(5,5));
		OsmQuest quest1 = new OsmQuest(null, new TestQuestType(), Element.Type.NODE, 11,
				QuestStatus.ANSWERED, null, new Date(1000), geometry);
		OsmQuest quest2 = new OsmQuest(null, new TestQuestType2(), Element.Type.NODE, 11,
				QuestStatus.ANSWERED, null, new Date(1000), geometry);

		geometryDao.put(quest1.getElementType(), quest1.getElementId(), geometry);
		dao.add(quest1);
		dao.add(quest2);

		assertEquals(1,dao.getAll(null, null, new TestQuestType(), null, null).size());
		assertEquals(2,dao.getAll(null, null, null, null, null).size());
	}

	public void testGetAllByElementTypeAndId()
	{
		ElementGeometry geometry = new ElementGeometry(new OsmLatLon(5,5));
		OsmQuest quest1 = new OsmQuest(null, new TestQuestType(), Element.Type.NODE, 11,
				QuestStatus.ANSWERED, null, new Date(1000), geometry);
		OsmQuest quest2 = new OsmQuest(null, new TestQuestType(), Element.Type.WAY, 12,
				QuestStatus.ANSWERED, null, new Date(1000), geometry);

		geometryDao.put(quest1.getElementType(), quest1.getElementId(), geometry);
		geometryDao.put(quest2.getElementType(), quest2.getElementId(), geometry);
		dao.add(quest1);
		dao.add(quest2);

		assertEquals(1,dao.getAll(null, null, null, Element.Type.NODE, null).size());
		assertEquals(1,dao.getAll(null, null, null, Element.Type.WAY, 12L).size());
	}

	private void checkEqual(OsmQuest quest, OsmQuest dbQuest)
	{
		assertEquals(quest.getId(), dbQuest.getId());
		assertEquals(quest.getType().getClass(), dbQuest.getType().getClass());
		assertEquals(quest.getElementId(), dbQuest.getElementId());
		assertEquals(quest.getElementType(), dbQuest.getElementType());
		assertEquals(quest.getStatus(), dbQuest.getStatus());
		assertEquals(quest.getChanges(), dbQuest.getChanges());
		assertEquals(quest.getGeometry(), dbQuest.getGeometry());
		assertEquals(quest.getMarkerLocation(), dbQuest.getMarkerLocation());
		assertEquals(quest.getLastUpdate(), dbQuest.getLastUpdate());
	}
}
