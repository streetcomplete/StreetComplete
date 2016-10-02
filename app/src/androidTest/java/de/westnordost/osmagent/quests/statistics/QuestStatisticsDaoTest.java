package de.westnordost.osmagent.quests.statistics;

import java.util.HashMap;

import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.quests.OsmagentDbTestCase;
import de.westnordost.osmagent.quests.QuestType;
import de.westnordost.osmagent.quests.osm.persist.test.TestQuestType;
import de.westnordost.osmagent.quests.osm.persist.test.TestQuestType2;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetParser;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.changesets.QueryChangesetsFilters;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.user.User;

public class QuestStatisticsDaoTest extends OsmagentDbTestCase
{
	private QuestStatisticsDao dao;

	private static final String ONE = "one";
	private static final String TWO = "two";

	@Override public void setUp()
	{
		super.setUp();
		dao = new QuestStatisticsDao(dbHelper, null);
	}

	private ChangesetInfo createOsmagentChangeset()
	{
		ChangesetInfo result = new ChangesetInfo();
		result.tags = new HashMap<>();
		result.tags.put("created_by", OsmagentConstants.USER_AGENT);
		return result;
	}


	public void testNothingToSync()
	{
		ChangesetInfo[] infos = {};
		dao = new QuestStatisticsDao(dbHelper, new TestChangesetsDao(infos));
		dao.syncFromOsmServer(0);
		assertEquals(0,dao.getTotalAmount());
	}

	public void testSyncEmptyChangesetNoError()
	{
		ChangesetInfo[] infos = {new ChangesetInfo()};
		dao = new QuestStatisticsDao(dbHelper, new TestChangesetsDao(infos));
		dao.syncFromOsmServer(0);
		assertEquals(0,dao.getTotalAmount());
	}

	public void testSync()
	{
		ChangesetInfo one = new ChangesetInfo();
		one.tags = new HashMap<>();
		one.tags.put("created_by", OsmagentConstants.USER_AGENT);
		one.tags.put(OsmagentConstants.QUESTTYPE_TAG_KEY, ONE);

		ChangesetInfo two = new ChangesetInfo();
		two.tags = new HashMap<>();
		two.tags.put("created_by", OsmagentConstants.USER_AGENT);
		two.tags.put(OsmagentConstants.QUESTTYPE_TAG_KEY, TWO);

		ChangesetInfo[] infos = {one, one, two};

		dao = new QuestStatisticsDao(dbHelper, new TestChangesetsDao(infos));
		dao.syncFromOsmServer(0);

		assertEquals(3,dao.getTotalAmount());
		assertEquals(2,dao.getAmount(ONE));
		assertEquals(1,dao.getAmount(TWO));
	}

	public void testGetZero()
	{
		assertEquals(0,dao.getAmount(ONE));
	}

	public void testGetOne()
	{
		dao.addOne(ONE);
		assertEquals(1,dao.getAmount(ONE));
	}

	public void testGetTwo()
	{
		dao.addOne(ONE);
		dao.addOne(ONE);
		assertEquals(2,dao.getAmount(ONE));
	}

	public void testGetTotal()
	{
		dao.addOne(ONE);
		dao.addOne(ONE);
		dao.addOne(TWO);
		assertEquals(3,dao.getTotalAmount());
	}

	private class TestChangesetsDao extends ChangesetsDao
	{

		private ChangesetInfo[] toReturn;

		public TestChangesetsDao(ChangesetInfo[] toReturn)
		{
			super(null);
			this.toReturn = toReturn;
		}

		@Override
		public void find(Handler<ChangesetInfo> handler, QueryChangesetsFilters filters)
		{
			for (ChangesetInfo aToReturn : toReturn)
			{
				handler.handle(aToReturn);
			}
		}
	}
}
