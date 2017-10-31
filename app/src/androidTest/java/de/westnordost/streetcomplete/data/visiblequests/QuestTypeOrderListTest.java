package de.westnordost.streetcomplete.data.visiblequests;


import android.content.Context;
import android.test.AndroidTestCase;


import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType2;

public class QuestTypeOrderListTest extends AndroidTestCase
{
	private QuestType testQuestType;
	private QuestType testQuestType2;

	private List<QuestType> list;

	private QuestTypeOrderList questTypeOrderList;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		testQuestType = new TestQuestType();
		testQuestType2 = new TestQuestType2();

		list = new ArrayList<>();
		list.add(testQuestType);
		list.add(testQuestType2);

		questTypeOrderList = new QuestTypeOrderList(
				getContext().getSharedPreferences("Test", Context.MODE_PRIVATE),
				new QuestTypeRegistry(list));
	}

	public void testSimpleReorder()
	{
		questTypeOrderList.apply(testQuestType2, testQuestType);
		questTypeOrderList.sort(list);

		assertEquals(testQuestType2, list.get(0));
		assertEquals(testQuestType, list.get(1));
	}

	public void testClear()
	{
		questTypeOrderList.apply(testQuestType2, testQuestType);
		questTypeOrderList.clear();
		questTypeOrderList.sort(list);

		assertEquals(testQuestType, list.get(0));
		assertEquals(testQuestType2, list.get(1));
	}
}
