package de.westnordost.streetcomplete.data.statistics;

import junit.framework.TestCase;

import java.util.Date;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.osmapi.user.UserInfo;

public class UserChangesetsDaoTest extends TestCase
{
	public void testAmount()
	{
		OsmConnection osm = new OsmConnection("https://master.apis.dev.openstreetmap.org/api/0.6/",
				"StreetComplete test case", null);
		long userId = 12;

		UserDao userDao = new UserDao(osm);
		UserInfo info = userDao.get(userId);
		UserChangesetsDao dao = new UserChangesetsDao(new ChangesetsDao(osm));
		Counter counter = new Counter();
		dao.findAll(counter, userId, new Date(0));

		assertEquals(info.changesetsCount, counter.count);
	}

	private class Counter implements Handler<ChangesetInfo>
	{
		int count = 0;
		@Override public void handle(ChangesetInfo tea)
		{
			count++;
		}
	}
}
