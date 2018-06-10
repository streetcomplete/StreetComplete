package de.westnordost.streetcomplete.quests.oneway;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.osm.persist.WayDao;
import de.westnordost.streetcomplete.util.Serializer;

public class WayTrafficFlowSegmentsDaoTest extends ApplicationDbTestCase
{
	private WayTrafficFlowDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dao = new WayTrafficFlowDao(dbHelper);
	}

	public void testPutGetTrue()
	{
		dao.put(123L, true);
		assertTrue(dao.isForward(123L));
	}

	public void testPutGetFalse()
	{
		dao.put(123L, false);
		assertFalse(dao.isForward(123L));
	}

	public void testGetNull()
	{
		assertNull(dao.isForward(123L));
	}

	public void testDelete()
	{
		dao.put(123L, false);
		dao.delete(123L);
		assertNull(dao.isForward(123L));
	}

	public void testOverwrite()
	{
		dao.put(123L, true);
		dao.put(123L, false);
		assertFalse(dao.isForward(123L));
	}

	public void testDeleteUnreferenced()
	{
		Serializer mockSerializer = new Serializer() {
			@Override public byte[] toBytes(Object object) { return new byte[0]; }
			@Override public <T> T toObject(byte[] bytes, Class<T> type)
			{
				try { return type.newInstance(); } catch (Exception e) { return null; }
			}
		};
		WayDao wayDao = new WayDao(dbHelper, mockSerializer);

		wayDao.put(new OsmWay(1, 0, new ArrayList<>(), null));
		wayDao.put(new OsmWay(2, 0, new ArrayList<>(), null));

		dao.put(1, true);
		dao.put(3, true);

		dao.deleteUnreferenced();

		assertTrue(dao.isForward(1));
		assertNull(dao.isForward(3));
	}
}
