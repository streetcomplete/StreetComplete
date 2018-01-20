package de.westnordost.streetcomplete.data.complete;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.complete.test.TestQuestType;

public class CompleteQuestDaoTest extends ApplicationDbTestCase
{
	private CompleteQuestDao dao;
	private ElementGeometryDao geometryDao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		List<QuestType> list = new ArrayList<>();
		list.add(new TestQuestType());

		geometryDao = new ElementGeometryDao(dbHelper, serializer);

		dao = new CompleteQuestDao(dbHelper, new QuestTypeRegistry(list), serializer);
	}

	private static Complete createComplete()
	{
		Complete complete = new Complete();
		complete.apiId = 2;
		complete.country = "DE";
		complete.completeType = CompleteTypes.CHART;
		complete.status = QuestStatus.NEW;

		return complete;
	}

	private static CompleteQuest createNewQuest(long id, Element.Type elementType)
	{
		return createNewQuest(id, elementType, new ElementGeometry(new OsmLatLon(5,5)));
	}

	private static CompleteQuest createNewQuest(long id, Element.Type elementType, ElementGeometry geometry)
	{
		return createNewQuest(new TestQuestType(), id, elementType, geometry);
	}

	private static CompleteQuest createNewQuest(CompleteQuestType questType, long id,
										   Element.Type elementType, ElementGeometry geometry)
	{
		return new CompleteQuest(null, createComplete(), QuestStatus.NEW, new Date(),
				questType, elementType, id, geometry);
	}

	private void addToDaos(CompleteQuest...quests)
	{
		for (CompleteQuest quest : quests)
		{
			geometryDao.put(quest.getElementType(), quest.getElementId(), quest.getGeometry());
			dao.add(quest);
		}
	}

	public void testAddGetNoChanges()
	{
		CompleteQuest quest = createNewQuest(11, Element.Type.NODE);

		addToDaos(quest);

		assertEquals(1, (long) quest.getId());
		CompleteQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	public void testAddGetWithChanges()
	{
		CompleteQuest quest = createNewQuest(11, Element.Type.NODE);
		quest.setAnswer("Yes");

		addToDaos(quest);

		assertEquals(1, (long) quest.getId());
		assertEquals("Yes", quest.getComplete().answer);
		CompleteQuest dbQuest = dao.get(1);

		checkEqual(quest, dbQuest);
	}

	public void testGetAllByBBox()
	{
		CompleteQuest quest1 = createNewQuest(11, Element.Type.NODE);
		CompleteQuest quest2 = createNewQuest(12, Element.Type.NODE, new ElementGeometry(new OsmLatLon(11,11)));

		addToDaos(quest1, quest2);

		assertEquals(1,dao.getAll(new BoundingBox(0,0,10,10), null).size());
		assertEquals(2,dao.getAll(null, null).size());
	}

	public void testGetAll()
	{
		CompleteQuest quest1 = createNewQuest(1, Element.Type.NODE);
		CompleteQuest quest2 = createNewQuest(2, Element.Type.NODE);

		addToDaos(quest1, quest2);

		List<CompleteQuest> list = dao.getAll(null, null);
		assertEquals(2, list.size());
	}

	private void checkEqual(CompleteQuest quest, CompleteQuest dbQuest)
	{
		assertEquals(quest.getStatus(), dbQuest.getStatus());
		assertEquals(quest.getMarkerLocation(), dbQuest.getMarkerLocation());
		assertEquals(quest.getComplete().answer, dbQuest.getComplete().answer);
		assertEquals(quest.getComplete().apiId, dbQuest.getComplete().apiId);
		assertEquals(quest.getId(), dbQuest.getId());
		assertEquals(quest.getType().getClass().getSimpleName(), dbQuest.getType().getClass().getSimpleName());
	}
}
