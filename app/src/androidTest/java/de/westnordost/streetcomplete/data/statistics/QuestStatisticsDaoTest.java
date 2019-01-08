package de.westnordost.streetcomplete.data.statistics;

import org.junit.Before;
import org.junit.Test;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

import static org.junit.Assert.*;

public class QuestStatisticsDaoTest extends ApplicationDbTestCase
{
	private QuestStatisticsDao dao;

	private static final String ONE = "one";
	private static final String TWO = "two";

	@Before public void createDao()
	{
		dao = new QuestStatisticsDao(dbHelper, null);
	}

	@Test public void getZero()
	{
		assertEquals(0,dao.getAmount(ONE));
	}

	@Test public void getOne()
	{
		dao.addOne(ONE);
		assertEquals(1,dao.getAmount(ONE));
	}

	@Test public void getTwo()
	{
		dao.addOne(ONE);
		dao.addOne(ONE);
		assertEquals(2,dao.getAmount(ONE));
	}

	@Test public void getTotal()
	{
		dao.addOne(ONE);
		dao.addOne(ONE);
		dao.addOne(TWO);
		assertEquals(3,dao.getTotalAmount());
	}
}
