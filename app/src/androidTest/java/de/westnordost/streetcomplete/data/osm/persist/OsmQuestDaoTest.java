package de.westnordost.streetcomplete.data.osm.persist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType2;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class OsmQuestDaoTest extends ApplicationDbTestCase
{
	private ElementGeometryDao geometryDao;
	private OsmQuestDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		geometryDao = new ElementGeometryDao(dbHelper, serializer);
		List<QuestType> list = new ArrayList<>();
		list.add(new TestQuestType());
		list.add(new TestQuestType2());

		dao = new OsmQuestDao(dbHelper, serializer, new QuestTypeRegistry(list));
	}

	public void testAddGetNoChanges()
	{
		OsmQuest quest = createNewQuest(11, Element.Type.NODE);

		addToDaos(quest);

		assertEquals(1, (long) quest.getId());
		OsmQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	public void testAddGetWithChanges()
	{
		List<StringMapEntryChange> changes = new ArrayList<>();
		changes.add(new StringMapEntryAdd("a key", "a value"));
		changes.add(new StringMapEntryDelete("delete this","key"));
		changes.add(new StringMapEntryModify("modify","this","to that"));
		OsmQuest quest = createNewQuest(11, Element.Type.NODE);
		quest.setChanges(new StringMapChanges(changes), "bla");

		addToDaos(quest);

		assertEquals(1, (long) quest.getId());
		OsmQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	public void testGetAllByBBox()
	{
		OsmQuest quest1 = createNewQuest(11, Element.Type.NODE);
		OsmQuest quest2 = createNewQuest(12, Element.Type.NODE, new ElementGeometry(new OsmLatLon(11,11)));

		addToDaos(quest1, quest2);

		assertEquals(1,dao.getAll(new BoundingBox(0,0,10,10), null, null, null, null).size());
		assertEquals(2,dao.getAll(null, null, null, null, null).size());
	}

	public void testGetAllByElementTypeAndId()
	{
		OsmQuest quest1 = createNewQuest(11, Element.Type.NODE);
		OsmQuest quest2 = createNewQuest(12, Element.Type.WAY);

		addToDaos(quest1, quest2);

		assertEquals(1,dao.getAll(null, null, null, Element.Type.NODE, null).size());
		assertEquals(1,dao.getAll(null, null, null, Element.Type.WAY, 12L).size());
	}

	public void testGetAllByMultipleQuestTypes()
	{
		ElementGeometry geom = new ElementGeometry(new OsmLatLon(5,5));

		OsmQuest quest1 = createNewQuest(new TestQuestType(), 1, Element.Type.NODE, geom);
		OsmQuest quest2 = createNewQuest(new TestQuestType2(), 2, Element.Type.NODE, geom);

		addToDaos(quest1, quest2);

		List<OsmQuest> only1 = dao.getAll(null, null, Collections.singletonList(
				TestQuestType.class.getSimpleName()));
		assertEquals(1,only1.size());
		List<OsmQuest> both = dao.getAll(null, null, Arrays.asList(
				TestQuestType.class.getSimpleName(),
				TestQuestType2.class.getSimpleName()));
		assertEquals(2, both.size());
	}

	private static OsmQuest createNewQuest(long id, Element.Type elementType)
	{
		return createNewQuest(id, elementType, new ElementGeometry(new OsmLatLon(5,5)));
	}

	private static OsmQuest createNewQuest(long id, Element.Type elementType, ElementGeometry geometry)
	{
		return createNewQuest(new TestQuestType(), id, elementType, geometry);
	}

	private static OsmQuest createNewQuest(OsmElementQuestType questType, long id,
										   Element.Type elementType, ElementGeometry geometry)
	{
		return new OsmQuest(null, questType, elementType, id,
				QuestStatus.ANSWERED, null, null, new Date(), geometry);
	}

	private void addToDaos(OsmQuest ...quests)
	{
		for (OsmQuest quest : quests)
		{
			geometryDao.put(quest.getElementType(), quest.getElementId(), quest.getGeometry());
			dao.add(quest);
		}
	}

	public void testDeleteAllClosed()
	{
		OsmQuest quest1 = createNewQuest(1, Element.Type.NODE);
		quest1.setStatus(QuestStatus.CLOSED);
		OsmQuest quest2 = createNewQuest(2, Element.Type.NODE);
		quest2.setStatus(QuestStatus.REVERT);
		OsmQuest quest3 = createNewQuest(3, Element.Type.NODE);

		addToDaos(quest1, quest2, quest3);

		assertEquals(2,dao.deleteAllClosed(System.currentTimeMillis() + 10000L));
	}

	public void testDeleteReverted()
	{
		ElementGeometry geom = new ElementGeometry(new OsmLatLon(5,5));

		OsmQuest quest1 = createNewQuest(new TestQuestType(), 1, Element.Type.NODE, geom);
		quest1.setStatus(QuestStatus.CLOSED);
		OsmQuest quest2 = createNewQuest(new TestQuestType2(), 1, Element.Type.NODE, geom);
		quest2.setStatus(QuestStatus.REVERT);
		OsmQuest quest3 = createNewQuest(new TestQuestType2(), 2, Element.Type.NODE, geom);
		quest3.setStatus(QuestStatus.REVERT);
		OsmQuest quest4 = createNewQuest(new TestQuestType2(), 1, Element.Type.WAY, geom);
		quest4.setStatus(QuestStatus.REVERT);

		addToDaos(quest1, quest2);

		assertEquals(1,dao.deleteAllReverted(Element.Type.NODE,1));
	}

	private void checkEqual(OsmQuest quest, OsmQuest dbQuest)
	{
		assertEquals(quest.getId(), dbQuest.getId());
		assertEquals(quest.getType().getClass(), dbQuest.getType().getClass());
		assertEquals(quest.getElementId(), dbQuest.getElementId());
		assertEquals(quest.getElementType(), dbQuest.getElementType());
		assertEquals(quest.getStatus(), dbQuest.getStatus());
		assertEquals(quest.getChanges(), dbQuest.getChanges());
		assertEquals(quest.getChangesSource(), dbQuest.getChangesSource());
		assertEquals(quest.getGeometry(), dbQuest.getGeometry());
		assertEquals(quest.getMarkerLocation(), dbQuest.getMarkerLocation());
		// is now updated to current time on DB insert
		// no: assertEquals(quest.getLastUpdate(), dbQuest.getLastUpdate());
	}
}
