package de.westnordost.osmagent.quests.statistics;

import de.westnordost.osmagent.quests.OsmagentDbTestCase;
import de.westnordost.osmagent.quests.QuestType;
import de.westnordost.osmagent.quests.osm.persist.test.TestQuestType;
import de.westnordost.osmagent.quests.osm.persist.test.TestQuestType2;

public class QuestStatisticsDaoTest extends OsmagentDbTestCase
{
	private QuestStatisticsDao dao;
	private QuestType one;
	private QuestType two;

	@Override public void setUp()
	{
		super.setUp();
		dao = new QuestStatisticsDao(dbHelper);
		one = new TestQuestType();
		two = new TestQuestType2();
	}

	public void testGetZero()
	{
		assertEquals(0,dao.getAmount(one));
	}

	public void testGetOne()
	{
		dao.addOne(one);
		assertEquals(1,dao.getAmount(one));
	}

	public void testGetTwo()
	{
		dao.addOne(one);
		dao.addOne(one);
		assertEquals(2,dao.getAmount(one));
	}

	public void testGetTotal()
	{
		dao.addOne(one);
		dao.addOne(one);
		dao.addOne(two);
		assertEquals(3,dao.getTotalAmount());
	}
}
