package de.westnordost.streetcomplete.data.visiblequests;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;

public class QuestTypeOrderList
{
	private final SharedPreferences prefs;
	private final QuestTypeRegistry questTypeRegistry;

	private List<List<String>> orderList;

	@Inject public QuestTypeOrderList(SharedPreferences prefs, QuestTypeRegistry questTypeRegistry)
	{
		this.prefs = prefs;
		this.questTypeRegistry = questTypeRegistry;
	}

	/** Apply and save a user defined order */
	public synchronized void apply(QuestType before, QuestType after)
	{
		List<List<String>> lists = get();
		applyOrderItemTo(before, after, lists);
		save(lists);
	}

	private static void applyOrderItemTo(QuestType before, QuestType after, List<List<String>> lists)
	{
		String beforeName = before.getClass().getSimpleName();
		String afterName = after.getClass().getSimpleName();
		int beforeIndices[] = findLocationInLists(beforeName, lists);
		int afterIndices[] = findLocationInLists(afterName, lists);

		// case 1: not in a list at all -> add in new list
		if(beforeIndices == null && afterIndices == null)
		{
			List<String> list = new ArrayList<>();
			list.add(beforeName);
			list.add(afterName);
			lists.add(list);
		}
		// case 2: one is in a list, the other not -> add other to the one list
		else if(afterIndices == null)
		{
			lists.get(beforeIndices[0]).add(beforeIndices[1]+1, afterName);
		}
		else if(beforeIndices == null)
		{
			lists.get(afterIndices[0]).add(afterIndices[1], beforeName);
		}
		// case 3: both in same list -> reorder that list
		else if(beforeIndices[0] == afterIndices[0])
		{
			List<String> list = lists.get(beforeIndices[0]);
			move(list, afterIndices[1], beforeIndices[1]+1);
		}
		// case 4: both in different lists -> put the whole other list into first list
		else
		{
			List<String> list = lists.get(afterIndices[0]);
			lists.get(beforeIndices[0]).addAll(beforeIndices[1]+1, list);
			lists.remove(afterIndices[0]);
		}
	}

	private static int[] findLocationInLists(String name, List<List<String>> lists)
	{
		for (int i = 0; i < lists.size(); i++)
		{
			List<String> names = lists.get(i);
			int idx = names.indexOf(name);
			if(idx != -1) return new int[]{i, idx};
		}
		return null;
	}

	/** Sort given list by the user defined order */
	public synchronized void sort(List<QuestType> questTypes)
	{
		List<List<QuestType>> orderLists = getAsQuestTypeLists();
		for (List<QuestType> list : orderLists)
		{
			List<QuestType> reorderedQuestTypes = new ArrayList<>(list.size()-1);
			for (QuestType questType : list.subList(1, list.size()))
			{
				if(questTypes.remove(questType))
				{
					reorderedQuestTypes.add(questType);
				}
			}

			int startIndex = questTypes.indexOf(list.get(0));
			questTypes.addAll(startIndex+1, reorderedQuestTypes);
		}
	}

	private static void move(List<?> list, int fromIndex, int toIndex)
	{
		if(fromIndex < toIndex)
		{
			Collections.rotate(list.subList(fromIndex, toIndex), -1);
		}
		else if(fromIndex > toIndex)
		{
			Collections.rotate(list.subList(toIndex, fromIndex+1), 1);
		}
	}

	private List<List<QuestType>> getAsQuestTypeLists()
	{
		List<List<String>> stringLists = get();
		List<List<QuestType>> result = new ArrayList<>(stringLists.size());
		for (List<String> stringList : stringLists)
		{
			List<QuestType> questTypes = new ArrayList<>(stringList.size());
			for (String string : stringList)
			{
				QuestType qt = questTypeRegistry.getByName(string);
				if(qt != null) questTypes.add(qt);
			}
			if(questTypes.size() > 1)
			{
				result.add(questTypes);
			}
		}
		return result;
	}

	private List<List<String>> get()
	{
		if(orderList == null)
		{
			orderList = load();
		}
		return orderList;
	}

	private static final String DELIM1 = ";", DELIM2 = ",";

	private List<List<String>> load()
	{
		String order = prefs.getString(Prefs.QUEST_ORDER, null);
		if(order != null)
		{
			String[] lists = order.split(DELIM1);
			List<List<String>> result = new ArrayList<>(lists.length);
			for (String list : lists)
			{
				result.add(new ArrayList<>(Arrays.asList(list.split(DELIM2))));
			}
			return result;
		}
		return new ArrayList<>();
	}

	private void save(List<List<String>> lists)
	{
		StringBuilder sb = new StringBuilder();
		boolean firstList = true;
		for (List<String> list : lists)
		{
			if(firstList) firstList = false;
			else sb.append(DELIM1);
			boolean firstName = true;
			for (String name : list)
			{
				if(firstName) firstName = false;
				else sb.append(DELIM2);
				sb.append(name);
			}
		}
		String order = sb.length() == 0 ? null : sb.toString();
		prefs.edit().putString(Prefs.QUEST_ORDER, order).apply();
	}

	public synchronized void clear()
	{
		prefs.edit().remove(Prefs.QUEST_ORDER).apply();
		orderList = null;
	}
}
