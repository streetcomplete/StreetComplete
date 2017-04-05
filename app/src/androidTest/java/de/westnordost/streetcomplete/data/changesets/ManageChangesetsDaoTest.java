package de.westnordost.streetcomplete.data.changesets;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

public class ManageChangesetsDaoTest  extends ApplicationDbTestCase
{
	private ManageChangesetsDao dao;

	private static final String Q = "Hurzipurz";
	private static final String P = "Brasliweks";

	@Override public void setUp()
	{
		super.setUp();
		dao = new ManageChangesetsDao(dbHelper);
	}

	public void testDeleteNonExistent()
	{
		assertFalse(dao.delete(Q));
	}

	public void testCreateDelete()
	{
		dao.setLastChangedToNow(Q);
		assertTrue(dao.delete(Q));
		assertNull(dao.get(Q));
	}

	public void testGetNull()
	{
		assertNull(dao.get(Q));
	}

	public void testInsertChangedTime()
	{
		long now = System.currentTimeMillis();
		dao.setLastChangedToNow(Q);
		ManageChangesetInfo info = dao.get(Q);
		assertEquals(null, info.changesetId);
		assertEquals(now, info.lastChanged );
		assertEquals(Q, info.questType);
	}

	public void testUpsertChangedTimeUpdatesTime() throws InterruptedException
	{
		dao.setLastChangedToNow(Q);
		synchronized (this)	{ wait(500); }
		long now = System.currentTimeMillis();
		dao.setLastChangedToNow(Q);
		assertEquals(now, dao.get(Q).lastChanged );
	}

	public void testUpsertChangedTimeDoesNotOverwriteChangesetId()
	{
		dao.assignChangesetId(Q, 1234);
		long now = System.currentTimeMillis();
		dao.setLastChangedToNow(Q);

		ManageChangesetInfo info = dao.get(Q);
		assertEquals(1234, (long) info.changesetId);
		assertEquals(now, info.lastChanged );
		assertEquals(Q, info.questType);
	}

	public void testInsertChangesetId()
	{
		dao.assignChangesetId(Q, 12);
		ManageChangesetInfo info = dao.get(Q);
		assertEquals(12, (long) info.changesetId);
		assertEquals(0, info.lastChanged ); // 0 is default
		assertEquals(Q, info.questType);
	}

	public void testUpsertChangesetId()
	{
		dao.assignChangesetId(Q, 12);
		dao.assignChangesetId(Q, 6497);
		assertEquals(6497, (long) dao.get(Q).changesetId);
	}

	public void testUpsertChangesetIdDoesNotOverwriteLastChangedTime()
	{
		long now = System.currentTimeMillis();
		dao.setLastChangedToNow(Q);
		dao.assignChangesetId(Q, 432);
		ManageChangesetInfo info = dao.get(Q);
		assertEquals(432, (long) info.changesetId);
		assertEquals(now, info.lastChanged);
		assertEquals(Q, info.questType);
	}

	public void testGetNone()
	{
		assertTrue(dao.getAll().isEmpty());
	}

	public void testInsertViaAssignChangesetId()
	{
		dao.assignChangesetId(Q,1);
		dao.assignChangesetId(P,2);
		assertEquals(2,dao.getAll().size());
	}

	public void testInsertViaSetLastChangedToNow()
	{
		dao.setLastChangedToNow(Q);
		dao.setLastChangedToNow(P);
		assertEquals(2,dao.getAll().size());
	}
}
