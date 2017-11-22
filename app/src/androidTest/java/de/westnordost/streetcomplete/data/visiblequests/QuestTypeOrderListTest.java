package de.westnordost.streetcomplete.data.visiblequests;


import android.content.Context;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType2;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType3;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType4;
import de.westnordost.streetcomplete.data.osm.persist.test.TestQuestType5;

import static org.assertj.core.api.Assertions.*;

public class QuestTypeOrderListTest extends AndroidTestCase
{
	private QuestType one;
	private QuestType two;
	private QuestType three;
	private QuestType four;
	private QuestType five;

	private List<QuestType> list;

	private QuestTypeOrderList questTypeOrderList;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		one = new TestQuestType();
		two = new TestQuestType2();
		three = new TestQuestType3();
		four = new TestQuestType4();
		five = new TestQuestType5();

		list = new ArrayList<>();
		list.addAll(Arrays.asList(one, two, three, four, five));

		questTypeOrderList = new QuestTypeOrderList(
				getContext().getSharedPreferences("Test", Context.MODE_PRIVATE),
				new QuestTypeRegistry(list));
		questTypeOrderList.clear();
	}

	public void testSimpleReorder()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one);
	}

	public void testTwoSeparateOrderLists()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.apply(five, four);

		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one);
		assertThat(list).containsSequence(five, four);
	}

	public void testExtendOrderList()
	{
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(three, two, one);
	}

	public void testExtendOrderListInReverse()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.apply(three, two);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(three, two, one);
	}

	public void testClear()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.clear();
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(one, two);
	}

	public void testQuestTypeInOrderListButNotInToBeSortedList()
	{
		list.remove(three);
		questTypeOrderList.apply(three, one);
		List<QuestType> before = new ArrayList<>(list);
		questTypeOrderList.sort(list);
		assertEquals(before, list);
	}

	public void testQuestTypeInOrderListButNotInToBeSortedListDoesNotInhibitSorting()
	{
		list.remove(three);
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one);
	}

	public void testQuestTypeOrderIsUpdatedCorrectlyMovedDown()
	{
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		// this now conflicts with the first statement -> should move the 3 after the 1
		questTypeOrderList.apply(one, three);

		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one, three);
	}

	public void testQuestTypeOrderIsUpdatedCorrectlyMovedUp()
	{
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		// this now conflicts with the first statement -> should move the 1 before the 2
		questTypeOrderList.apply(three, one);

		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(three, one, two);
	}

	public void testPickQuestTypeOrders()
	{
		questTypeOrderList.apply(four, three);
		questTypeOrderList.apply(two, one);
		questTypeOrderList.apply(one, five);

		// merging the two here..
		questTypeOrderList.apply(one, three);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(two, one, three, five);
	}

	public void testMergeQuestTypeOrders()
	{
		questTypeOrderList.apply(four, three);
		questTypeOrderList.apply(two, one);

		// merging the two here..
		questTypeOrderList.apply(three, two);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(four, three, two, one);
	}

	public void testReorderFirstItemToBackOfSameList()
	{
		questTypeOrderList.apply(one, two);
		questTypeOrderList.apply(two, three);
		questTypeOrderList.apply(three, four);

		questTypeOrderList.apply(four, one);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(two, three, four, one);
	}

	public void testReorderAnItemToBackOfSameList()
	{
		questTypeOrderList.apply(one, two);
		questTypeOrderList.apply(two, three);
		questTypeOrderList.apply(three, four);

		questTypeOrderList.apply(four, two);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(one, three, four, two);
	}
}
