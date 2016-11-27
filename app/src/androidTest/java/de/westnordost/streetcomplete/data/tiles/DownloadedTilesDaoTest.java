package de.westnordost.streetcomplete.data.tiles;

import android.graphics.Rect;

import java.util.List;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

public class DownloadedTilesDaoTest extends ApplicationDbTestCase
{
	private DownloadedTilesDao dao;

	@Override public void setUp()
	{
		super.setUp();
		dao = new DownloadedTilesDao(dbHelper);
	}

	public void testPutGetOne()
	{
		dao.putQuestType(new Rect(5,8,5,8), "Huhu");
		List<String> huhus = dao.getQuestTypeNames(new Rect(5,8,5,8),0);

		assertEquals(1, huhus.size());
		assertTrue(huhus.contains("Huhu"));
	}

	public void testPutGetOld()
	{
		dao.putQuestType(new Rect(5,8,5,8), "Huhu");
		List<String> huhus = dao.getQuestTypeNames(new Rect(5,8,5,8),System.currentTimeMillis() + 1000);
		assertTrue(huhus.isEmpty());
	}

	public void testPutMoreGetOne()
	{
		dao.putQuestType(new Rect(5,8,6,10), "Huhu");
		assertFalse(dao.getQuestTypeNames(new Rect(5,8,5,8),0).isEmpty());
		assertFalse(dao.getQuestTypeNames(new Rect(6,10,6,10),0).isEmpty());
	}

	public void testPutOneGetMore()
	{
		dao.putQuestType(new Rect(5,8,5,8), "Huhu");
		assertTrue(dao.getQuestTypeNames(new Rect(5,8,5,9),0).isEmpty());
	}

	public void testPutSeveralQuestTypes()
	{
		dao.putQuestType(new Rect(0,0,5,5), "Huhu");
		dao.putQuestType(new Rect(4,4,6,6), "hoho");
		dao.putQuestType(new Rect(4,0,4,7), "hihi");

		List<String> check = dao.getQuestTypeNames(new Rect(0,0,2,2),0);
		assertEquals(1, check.size());
		assertTrue(check.contains("Huhu"));

		check = dao.getQuestTypeNames(new Rect(4,4,4,4),0);
		assertEquals(3, check.size());

		check = dao.getQuestTypeNames(new Rect(5,5,5,5),0);
		assertEquals(2, check.size());
		assertTrue(check.contains("hoho"));
		assertTrue(check.contains("Huhu"));

		check = dao.getQuestTypeNames(new Rect(0,0,6,6),0);
		assertTrue(check.isEmpty());
	}
}
