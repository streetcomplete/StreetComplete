package de.westnordost.streetcomplete.data.visiblequests;

import org.junit.Before;
import org.junit.Test;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.osm.persist.test.DisabledTestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType;

import static org.junit.Assert.*;

public class VisibleQuestTypeDaoTest extends ApplicationDbTestCase
{
	private VisibleQuestTypeDao dao;

	private TestQuestType testQuestType = new TestQuestType();
	private DisabledTestQuestType disabledTestQuestType = new DisabledTestQuestType();

	@Before public void createDao()
	{
		dao = new VisibleQuestTypeDao(dbHelper);
	}

	@Test public void defaultEnabledQuest()
	{
		assertTrue(dao.isVisible(testQuestType));
	}

	@Test public void defaultDisabledQuests()
	{
		assertFalse(dao.isVisible(disabledTestQuestType));
	}

	@Test public void disableQuest()
	{
		dao.setVisible(testQuestType, false);
		assertFalse(dao.isVisible(testQuestType));
	}

	@Test public void enableQuest()
	{
		dao.setVisible(disabledTestQuestType, true);
		assertTrue(dao.isVisible(disabledTestQuestType));
	}

	@Test public void reset()
	{
		dao.setVisible(testQuestType, false);
		assertFalse(dao.isVisible(testQuestType));
		dao.clear();
		assertTrue(dao.isVisible(testQuestType));
	}
}
