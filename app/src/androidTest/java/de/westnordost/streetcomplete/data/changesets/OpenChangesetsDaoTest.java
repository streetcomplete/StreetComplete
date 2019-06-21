package de.westnordost.streetcomplete.data.changesets;

import org.junit.Before;
import org.junit.Test;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

import static org.junit.Assert.*;

public class OpenChangesetsDaoTest extends ApplicationDbTestCase
{
	private OpenChangesetsDao dao;

	private static final OpenChangesetKey Q = new OpenChangesetKey("Hurzipurz","test");
	private static final OpenChangesetKey P = new OpenChangesetKey("Brasliweks","test");

	@Before public void createDao()
	{
		dao = new OpenChangesetsDao(dbHelper, null);
	}

	@Test public void deleteNonExistent()
	{
		assertFalse(dao.delete(Q));
	}

	@Test public void createDelete()
	{
		dao.replace(Q,1);
		assertTrue(dao.delete(Q));
		assertNull(dao.get(Q));
	}

	@Test public void getNull()
	{
		assertNull(dao.get(Q));
	}

	@Test public void insertChangesetId()
	{
		dao.replace(Q, 12);
		OpenChangesetInfo info = dao.get(Q);
		assertEquals(12, (long) info.changesetId);
		assertEquals(Q.questType, info.key.questType);
		assertEquals(Q.source, info.key.source);
	}

	@Test public void replaceChangesetId()
	{
		dao.replace(Q, 12);
		dao.replace(Q, 6497);
		assertEquals(6497, (long) dao.get(Q).changesetId);
	}

	@Test public void getNone()
	{
		assertTrue(dao.getAll().isEmpty());
	}

	@Test public void insertTwo()
	{
		dao.replace(Q,1);
		dao.replace(P,2);
		assertEquals(2,dao.getAll().size());
	}
}
