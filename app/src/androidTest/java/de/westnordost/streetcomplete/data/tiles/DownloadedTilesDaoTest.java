package de.westnordost.streetcomplete.data.tiles;

import android.graphics.Point;
import android.graphics.Rect;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

import static org.junit.Assert.*;

public class DownloadedTilesDaoTest extends ApplicationDbTestCase
{
	private DownloadedTilesDao dao;

	@Before public void createDao()
	{
		dao = new DownloadedTilesDao(dbHelper);
	}

	@Test public void putGetOne()
	{
		dao.put(new Rect(5,8,5,8), "Huhu");
		List<String> huhus = dao.get(new Rect(5,8,5,8),0);

		assertEquals(1, huhus.size());
		assertTrue(huhus.contains("Huhu"));
	}

	@Test public void putGetOld()
	{
		dao.put(new Rect(5,8,5,8), "Huhu");
		List<String> huhus = dao.get(new Rect(5,8,5,8),System.currentTimeMillis() + 1000);
		assertTrue(huhus.isEmpty());
	}

	@Test public void putSomeOld() throws InterruptedException
	{
		dao.put(new Rect(0,0,1,3), "Huhu");
		Thread.sleep(2000);
		dao.put(new Rect(1,3,5,5), "Huhu");
		List<String> huhus = dao.get(new Rect(0,0,2,2),System.currentTimeMillis() - 1000);
		assertTrue(huhus.isEmpty());
	}

	@Test public void putMoreGetOne()
	{
		dao.put(new Rect(5,8,6,10), "Huhu");
		assertFalse(dao.get(new Rect(5,8,5,8),0).isEmpty());
		assertFalse(dao.get(new Rect(6,10,6,10),0).isEmpty());
	}

	@Test public void putOneGetMore()
	{
		dao.put(new Rect(5,8,5,8), "Huhu");
		assertTrue(dao.get(new Rect(5,8,5,9),0).isEmpty());
	}

	@Test public void remove()
	{
		dao.put(new Rect(0,0,3,3), "Huhu");
		dao.put(new Rect(0,0,0,0), "Haha");
		dao.put(new Rect(1,1,3,3), "Hihi");
		assertEquals(2, dao.remove(new Point(0,0))); // removes huhu, haha at 0,0
	}

	@Test public void putSeveralQuestTypes()
	{
		dao.put(new Rect(0,0,5,5), "Huhu");
		dao.put(new Rect(4,4,6,6), "hoho");
		dao.put(new Rect(4,0,4,7), "hihi");

		List<String> check = dao.get(new Rect(0,0,2,2),0);
		assertEquals(1, check.size());
		assertTrue(check.contains("Huhu"));

		check = dao.get(new Rect(4,4,4,4),0);
		assertEquals(3, check.size());

		check = dao.get(new Rect(5,5,5,5),0);
		assertEquals(2, check.size());
		assertTrue(check.contains("hoho"));
		assertTrue(check.contains("Huhu"));

		check = dao.get(new Rect(0,0,6,6),0);
		assertTrue(check.isEmpty());
	}
}
