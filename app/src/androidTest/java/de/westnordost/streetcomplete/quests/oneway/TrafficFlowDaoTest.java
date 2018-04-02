package de.westnordost.streetcomplete.quests.oneway;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

public class TrafficFlowDaoTest extends ApplicationDbTestCase
{
	private TrafficFlowDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dao = new TrafficFlowDao(dbHelper);
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

	public void testRemove()
	{
		dao.put(123L, false);
		dao.remove(123L);
		assertNull(dao.isForward(123L));
	}

	public void testOverwrite()
	{
		dao.put(123L, true);
		dao.put(123L, false);
		assertFalse(dao.isForward(123L));
	}
}
