package de.westnordost.streetcomplete.data.visiblequests;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.osm.persist.test.DisabledTestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType;

public class VisibleQuestTypeDaoTest extends ApplicationDbTestCase
{
	private VisibleQuestTypeDao dao;
	private TestQuestType testQuestType = new TestQuestType();
	private DisabledTestQuestType disabledTestQuestType = new DisabledTestQuestType();

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dao = new VisibleQuestTypeDao(dbHelper);
	}

	public void testDefaultEnabledQuest()
	{
		assertTrue(dao.isVisible(testQuestType));
	}

	public void testDefaultDisabledQuests()
	{
		assertFalse(dao.isVisible(disabledTestQuestType));
	}

	public void testDisableQuest()
	{
		dao.setVisible(testQuestType, false);
		assertFalse(dao.isVisible(testQuestType));
	}

	public void testEnableQuest()
	{
		dao.setVisible(disabledTestQuestType, true);
		assertTrue(dao.isVisible(disabledTestQuestType));
	}

	public void testReset()
	{
		dao.setVisible(testQuestType, false);
		assertFalse(dao.isVisible(testQuestType));
		dao.clear();
		assertTrue(dao.isVisible(testQuestType));
	}
}
