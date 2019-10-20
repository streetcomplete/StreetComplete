package de.westnordost.streetcomplete.data.visiblequests;


import android.content.Context;

import org.junit.Before;
import org.junit.Test;

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

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;

public class QuestTypeOrderListTest
{
	private QuestType one = new TestQuestType();
	private QuestType two = new TestQuestType2();
	private QuestType three = new TestQuestType3();
	private QuestType four = new TestQuestType4();
	private QuestType five = new TestQuestType5();

	private List<QuestType> list;

	private QuestTypeOrderList questTypeOrderList;

	@Before public void setUpList()
	{
		list = new ArrayList<>();
		list.addAll(Arrays.asList(one, two, three, four, five));

		questTypeOrderList = new QuestTypeOrderList(
				getInstrumentation().getContext().getSharedPreferences("Test", Context.MODE_PRIVATE),
				new QuestTypeRegistry(list));
		questTypeOrderList.clear();
	}

	@Test public void simpleReorder()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one);
	}

	@Test public void twoSeparateOrderLists()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.apply(five, four);

		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one);
		assertThat(list).containsSequence(five, four);
	}

	@Test public void extendOrderList()
	{
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(three, two, one);
	}

	@Test public void extendOrderListInReverse()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.apply(three, two);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(three, two, one);
	}

	@Test public void clear()
	{
		questTypeOrderList.apply(two, one);
		questTypeOrderList.clear();
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(one, two);
	}

	@Test public void questTypeInOrderListButNotInToBeSortedList()
	{
		list.remove(three);
		questTypeOrderList.apply(three, one);
		List<QuestType> before = new ArrayList<>(list);
		questTypeOrderList.sort(list);
		assertEquals(before, list);
	}

	@Test public void questTypeInOrderListButNotInToBeSortedListDoesNotInhibitSorting()
	{
		list.remove(three);
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one);
	}

	@Test public void questTypeOrderIsUpdatedCorrectlyMovedDown()
	{
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		// this now conflicts with the first statement -> should move the 3 after the 1
		questTypeOrderList.apply(one, three);

		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(two, one, three);
	}

	@Test public void questTypeOrderIsUpdatedCorrectlyMovedUp()
	{
		questTypeOrderList.apply(three, two);
		questTypeOrderList.apply(two, one);
		// this now conflicts with the first statement -> should move the 1 before the 2
		questTypeOrderList.apply(three, one);

		questTypeOrderList.sort(list);

		assertThat(list).containsSequence(three, one, two);
	}

	@Test public void pickQuestTypeOrders()
	{
		questTypeOrderList.apply(four, three);
		questTypeOrderList.apply(two, one);
		questTypeOrderList.apply(one, five);

		// merging the two here..
		questTypeOrderList.apply(one, three);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(two, one, three, five);
	}

	@Test public void mergeQuestTypeOrders()
	{
		questTypeOrderList.apply(four, three);
		questTypeOrderList.apply(two, one);

		// merging the two here..
		questTypeOrderList.apply(three, two);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(four, three, two, one);
	}

	@Test public void reorderFirstItemToBackOfSameList()
	{
		questTypeOrderList.apply(one, two);
		questTypeOrderList.apply(two, three);
		questTypeOrderList.apply(three, four);

		questTypeOrderList.apply(four, one);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(two, three, four, one);
	}

	@Test public void reorderAnItemToBackOfSameList()
	{
		questTypeOrderList.apply(one, two);
		questTypeOrderList.apply(two, three);
		questTypeOrderList.apply(three, four);

		questTypeOrderList.apply(four, two);

		questTypeOrderList.sort(list);
		assertThat(list).containsSequence(one, three, four, two);
	}
}
