package de.westnordost.streetcomplete.data.changesets;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

public class OpenChangesetsDaoTest extends ApplicationDbTestCase
{
	private OpenChangesetsDao dao;

	private static final String Q = "Hurzipurz";
	private static final String P = "Brasliweks";

	@Override public void setUp()
	{
		super.setUp();
		dao = new OpenChangesetsDao(dbHelper);
	}

	public void testDeleteNonExistent()
	{
		assertFalse(dao.delete(Q));
	}

	public void testCreateDelete()
	{
		dao.replace(Q,1);
		assertTrue(dao.delete(Q));
		assertNull(dao.get(Q));
	}

	public void testGetNull()
	{
		assertNull(dao.get(Q));
	}

	public void testInsertChangesetId()
	{
		dao.replace(Q, 12);
		OpenChangesetInfo info = dao.get(Q);
		assertEquals(12, (long) info.changesetId);
		assertEquals(Q, info.questType);
	}

	public void testReplaceChangesetId()
	{
		dao.replace(Q, 12);
		dao.replace(Q, 6497);
		assertEquals(6497, (long) dao.get(Q).changesetId);
	}

	public void testGetNone()
	{
		assertTrue(dao.getAll().isEmpty());
	}

	public void testInsertTwo()
	{
		dao.replace(Q,1);
		dao.replace(P,2);
		assertEquals(2,dao.getAll().size());
	}
}
