package de.westnordost.streetcomplete.data.osm.persist;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

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
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType3;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType4;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType5;

public class OsmQuestDaoTest extends ApplicationDbTestCase
{
	private ElementGeometryDao geometryDao;
	private OsmQuestDao dao;

	@Before public void createDaos()
	{
		geometryDao = new ElementGeometryDao(dbHelper, serializer);
		List<QuestType> list = Arrays.asList(
			new TestQuestType(),
			new TestQuestType2(),
			new TestQuestType3(),
			new TestQuestType4(),
			new TestQuestType5()
		);

		dao = new OsmQuestDao(dbHelper, serializer, new QuestTypeRegistry(list));
	}

	@Test public void addGetNoChanges()
	{
		OsmQuest quest = createNewQuest(11, Element.Type.NODE);

		addToDaos(quest);

		assertEquals(1, (long) quest.getId());
		OsmQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	@Test public void addGetWithChanges()
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

	@Test public void getAllByBBox()
	{
		OsmQuest quest1 = createNewQuest(11, Element.Type.NODE);
		OsmQuest quest2 = createNewQuest(12, Element.Type.NODE, new ElementGeometry(new OsmLatLon(11,11)));

		addToDaos(quest1, quest2);

		assertEquals(1,dao.getAll(new BoundingBox(0,0,10,10), null, null, null, null).size());
		assertEquals(2,dao.getAll(null, null, null, null, null).size());
	}

	@Test public void getAllByElementTypeAndId()
	{
		OsmQuest quest1 = createNewQuest(11, Element.Type.NODE);
		OsmQuest quest2 = createNewQuest(12, Element.Type.WAY);

		addToDaos(quest1, quest2);

		assertEquals(1,dao.getAll(null, null, null, Element.Type.NODE, null).size());
		assertEquals(1,dao.getAll(null, null, null, Element.Type.WAY, 12L).size());
	}

	@Test public void getAllByMultipleQuestTypes()
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
				QuestStatus.NEW, null, null, new Date(), geometry);
	}

	private void addToDaos(OsmQuest ...quests)
	{
		for (OsmQuest quest : quests)
		{
			geometryDao.put(quest.getElementType(), quest.getElementId(), quest.getGeometry());
			boolean result = dao.add(quest);
			assertTrue(result);
		}
	}

	@Test public void deleteAllClosed()
	{
		OsmQuest quest1 = createNewQuest(1, Element.Type.NODE);
		quest1.setStatus(QuestStatus.CLOSED);
		OsmQuest quest2 = createNewQuest(2, Element.Type.NODE);
		quest2.setStatus(QuestStatus.REVERT);
		OsmQuest quest3 = createNewQuest(3, Element.Type.NODE);

		addToDaos(quest1, quest2, quest3);

		assertEquals(2,dao.deleteAllClosed(System.currentTimeMillis() + 10000L));
	}

	@Test public void deleteAllUnsolved()
	{
		OsmQuest quest1 = createNewQuest(1, Element.Type.NODE);
		quest1.setStatus(QuestStatus.NEW);
		OsmQuest quest2 = createNewQuest(2, Element.Type.NODE);
		quest2.setStatus(QuestStatus.HIDDEN);
		OsmQuest quest3 = createNewQuest(3, Element.Type.NODE);
		quest3.setStatus(QuestStatus.ANSWERED);

		addToDaos(quest1, quest2, quest3);

		assertEquals(2,dao.deleteAllUnsolved(System.currentTimeMillis() + 10000L));
	}

	@Test public void dDeleteReverted()
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

	@Test public void getNextNewAt()
	{
		ElementGeometry geom = new ElementGeometry(new OsmLatLon(5,5));

		OsmQuest quest = createNewQuest(new TestQuestType(), 1, Element.Type.NODE, geom);
		quest.setStatus(QuestStatus.ANSWERED);

		addToDaos(quest,
				createNewQuest(new TestQuestType2(), 1, Element.Type.NODE, geom),
				createNewQuest(new TestQuestType3(), 1, Element.Type.NODE, geom),
				createNewQuest(new TestQuestType4(), 1, Element.Type.NODE, geom),
				createNewQuest(new TestQuestType5(), 1, Element.Type.NODE, geom));

		OsmQuest nextQuest = dao.getNextNewAt(1,
				Arrays.asList("TestQuestType4", "TestQuestType3", "TestQuestType5"));

		assertEquals(nextQuest.getType().getClass().getSimpleName(),"TestQuestType4");
	}

	@Test public void getNoFittingNextNewAt()
	{
		ElementGeometry geom = new ElementGeometry(new OsmLatLon(5,5));

		OsmQuest quest = createNewQuest(new TestQuestType(), 1, Element.Type.NODE, geom);
		quest.setStatus(QuestStatus.ANSWERED);

		addToDaos(quest,createNewQuest(new TestQuestType2(), 2, Element.Type.NODE, geom));

		assertNull(dao.getNextNewAt(1,Arrays.asList("TestQuestType")));
	}

	@Test public void getNoNextNewAt()
	{
		ElementGeometry geom = new ElementGeometry(new OsmLatLon(5,5));

		OsmQuest quest = createNewQuest(new TestQuestType(), 1, Element.Type.NODE, geom);
		quest.setStatus(QuestStatus.ANSWERED);

		addToDaos(quest);

		assertNull(dao.getNextNewAt(1,null));
	}

	@Test public void getAllIds()
	{
		ElementGeometry geom = new ElementGeometry(new OsmLatLon(5,5));

		OsmQuest q1 = createNewQuest(new TestQuestType(), 1, Element.Type.NODE, geom);
		OsmQuest q2 = createNewQuest(new TestQuestType2(), 1, Element.Type.NODE, geom);
		OsmQuest q3 = createNewQuest(new TestQuestType3(), 3, Element.Type.NODE, geom);
		OsmQuest q4 = createNewQuest(new TestQuestType4(), 1, Element.Type.WAY, geom);
		addToDaos(q1,q2,q3,q4);

		List<Long> ids = dao.getAllIds(Element.Type.NODE, 1);
		assertThat(ids).containsExactlyInAnyOrder(q1.getId(), q2.getId());
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
		assertEquals(quest.getCenter(), dbQuest.getCenter());
		// is now updated to current time on DB insert
		// no: assertEquals(quest.getLastUpdate(), dbQuest.getLastUpdate());
	}
}
