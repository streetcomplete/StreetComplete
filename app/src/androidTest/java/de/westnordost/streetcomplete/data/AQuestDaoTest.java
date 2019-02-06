package de.westnordost.streetcomplete.data;

import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class AQuestDaoTest
{
	private TestQuestDao dao;
	private SQLiteOpenHelper dbHelper;

	@Before public void setUpHelper()
	{
		dbHelper = new TestQuestDao.TestDbHelper(getInstrumentation().getTargetContext());
		dao = new TestQuestDao(dbHelper);
	}

	@After public void tearDownHelper()
	{
		dbHelper.close();
		getInstrumentation().getTargetContext().deleteDatabase(TestQuestDao.TESTDB);
	}

	@Test public void addGet()
	{
		long id = 3;
		Quest q = createQuest(id,0,0, QuestStatus.HIDDEN);
		dao.add(q);
		Quest q2 = dao.get(id);

		assertEquals(q.getId(), q2.getId());
		assertEquals(q.getCenter(), q2.getCenter());
		assertEquals(q.getStatus(), q2.getStatus());
	}

	@Test public void addAll()
	{
		Collection<Quest> quests = new ArrayList<>();

		quests.add(createQuest(3,0,0, QuestStatus.NEW));
		quests.add(createQuest(4,0,0, QuestStatus.ANSWERED));

		assertEquals(2,dao.addAll(quests));
	}

	@Test public void addAllNoOverwrite()
	{
		Collection<Quest> quests = new ArrayList<>();

		quests.add(createQuest(3,0,0, QuestStatus.NEW));
		quests.add(createQuest(3,0,0, QuestStatus.ANSWERED));

		assertEquals(1,dao.addAll(quests));
	}

	@Test public void addNoOverwrite()
	{
		assertTrue(dao.add(createQuest(3,0,0, QuestStatus.HIDDEN)));
		assertFalse(dao.add(createQuest(3,0,0, QuestStatus.NEW)));

		assertEquals(QuestStatus.HIDDEN, dao.get(3).getStatus());
	}

	@Test public void replace()
	{
		assertTrue(dao.add(createQuest(3,0,0, QuestStatus.HIDDEN)));
		assertTrue(dao.replace(createQuest(3,0,0, QuestStatus.NEW)));

		assertEquals(QuestStatus.NEW, dao.get(3).getStatus());
		assertEquals(1,dao.getAll(null,null).size());
	}

	@Test public void delete()
	{
		assertFalse(dao.delete(0));
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		assertTrue(dao.delete(1));
	}

	@Test public void deleteAll()
	{
		dao.add(createQuest(0,0,0, QuestStatus.NEW));
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		dao.add(createQuest(2,0,0, QuestStatus.NEW));
		assertEquals(2, dao.deleteAll(Arrays.asList(1L, 2L)));
	}

	@Test public void update()
	{
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		dao.update(createQuest(1,0,0, QuestStatus.ANSWERED));
		assertEquals(QuestStatus.ANSWERED, dao.get(1).getStatus());
	}

	@Test public void getAllByBoundingBox()
	{
		BoundingBox bbox = new BoundingBox(50,1,51,2);

		// on border
		dao.add(createQuest(1,50,1, QuestStatus.NEW));
		// right lat but wrong lon
		dao.add(createQuest(2,50.5,50.5, QuestStatus.NEW));
		// wrong lat but right lon
		dao.add(createQuest(3,1.5,1.5, QuestStatus.NEW));
		// in
		dao.add(createQuest(4,50.5,1.5, QuestStatus.NEW));

		List<Quest> quests = dao.getAll(bbox, QuestStatus.NEW);
		Collections.sort(quests, (lhs, rhs) -> (int) (lhs.getId() - rhs.getId()));
		assertEquals(2,quests.size());
		assertEquals(1,(long) quests.get(0).getId());
		assertEquals(4,(long) quests.get(1).getId());

		assertEquals(2, dao.getCount(bbox, QuestStatus.NEW));
	}

	@Test public void getCountWhenEmpty()
	{
		assertEquals(0, dao.getCount(new BoundingBox(50,1,51,2), QuestStatus.NEW));
	}

	@Test public void getLastSolvedContainsOnlySolvedQuests()
	{
		dao.add(createQuest(0,0, QuestStatus.NEW));
		dao.add(createQuest(2,0, QuestStatus.INVISIBLE));
		assertNull(dao.getLastSolved());
	}

	@Test public void getLastSolvedIncludesClosedQuests()
	{
		dao.add(createQuest(0,0, QuestStatus.CLOSED));
		assertNotNull(dao.getLastSolved());
	}

	@Test public void getLastSolvedIncludesAnsweredQuests()
	{
		dao.add(createQuest(1,0, QuestStatus.ANSWERED));
		assertNotNull(dao.getLastSolved());
	}

	@Test public void getLastSolvedIncludesHiddenQuests()
	{
		dao.add(createQuest(1,0, QuestStatus.HIDDEN));
		assertNotNull(dao.getLastSolved());
	}

	@Test public void getLastSolvedDoesNotIncludeRevertedQuests()
	{
		dao.add(createQuest(0,0, QuestStatus.REVERT));
		assertNull(dao.getLastSolved());
	}

	@Test public void getLastSolvedSortsByLastUpdate()
	{
		dao.add(createQuest(0,10000, QuestStatus.ANSWERED));
		dao.add(createQuest(1,20000, QuestStatus.CLOSED));
		assertEquals(1L, (long) dao.getLastSolved().getId());
	}

	private static Quest createQuest(long id, long lastUpdate, QuestStatus status)
	{
		Quest quest = mock(Quest.class);
		when(quest.getId()).thenReturn(id);
		when(quest.getStatus()).thenReturn(status);
		when(quest.getLastUpdate()).thenReturn(new Date(lastUpdate));
		when(quest.getCenter()).thenReturn(new OsmLatLon(0,0));
		return quest;
	}

	private static Quest createQuest(long id, double lat, double lon, QuestStatus status)
	{
		Quest quest = mock(Quest.class);
		when(quest.getStatus()).thenReturn(status);
		when(quest.getId()).thenReturn(id);
		when(quest.getCenter()).thenReturn(new OsmLatLon(lat,lon));
		when(quest.getLastUpdate()).thenReturn(new Date());
		return quest;
	}
}
