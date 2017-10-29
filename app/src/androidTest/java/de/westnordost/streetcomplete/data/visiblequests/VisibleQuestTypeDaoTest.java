package de.westnordost.streetcomplete.data.visiblequests;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.persist.test.DisabledTestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType2;

public class VisibleQuestTypeDaoTest extends ApplicationDbTestCase
{
	private VisibleQuestTypeDao dao;
	private TestQuestType testQuestType = new TestQuestType();
	private TestQuestType2 testQuestType2 = new TestQuestType2();
	private DisabledTestQuestType disabledTestQuestType = new DisabledTestQuestType();

	@Override public void setUp()
	{
		super.setUp();
		List<QuestType> list = new ArrayList<>();
		list.add(testQuestType);
		list.add(testQuestType2);
		list.add(disabledTestQuestType);
		dao = new VisibleQuestTypeDao(dbHelper, new QuestTypeRegistry(list));
	}

	public void testDefaultDisabledQuests()
	{
		assertEquals(2, dao.getAll().size());
	}

	public void testEnableDefaultDisabledQuest()
	{
		dao.setVisible(disabledTestQuestType, true);
		assertEquals(3, dao.getAll().size());
	}

	public void testDisableQuest()
	{
		dao.setVisible(testQuestType, false);
		List<QuestType> questTypes = dao.getAll();
		assertEquals(1, questTypes.size());
		assertEquals(testQuestType2, questTypes.get(0));
	}
}
