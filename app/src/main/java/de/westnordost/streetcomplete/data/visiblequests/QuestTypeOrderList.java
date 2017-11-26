package de.westnordost.streetcomplete.data.visiblequests;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
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

		// 1. remove after-item from the list it is in
		List<String> afterList = findListThatContains(afterName, lists);

		List<String> afterNames = new ArrayList<>(2);
		afterNames.add(afterName);

		if(afterList != null)
		{
			int afterIndex = afterList.indexOf(afterName);
			List<String> beforeList = findListThatContains(beforeName, lists);
			// if it is the head of a list, transplant the whole list
			if(afterIndex == 0 && afterList != beforeList)
			{
				afterNames = afterList;
				lists.remove(afterList);
			}
			else
			{
				afterList.remove(afterIndex);
				// remove that list if it became too small to be meaningful
				if(afterList.size() < 2) lists.remove(afterList);
			}
		}

		// 2. add it/them back to a list after before-item
		List<String> beforeList = findListThatContains(beforeName, lists);

		if(beforeList != null)
		{
			int beforeIndex = beforeList.indexOf(beforeName);
			beforeList.addAll(beforeIndex+1, afterNames);
		}
		else
		{
			List<String> list = new ArrayList<>();
			list.add(beforeName);
			list.addAll(afterNames);
			lists.add(list);
		}
	}

	private static List<String> findListThatContains(String name, List<List<String>> lists)
	{
		for (int i = 0; i < lists.size(); i++)
		{
			List<String> names = lists.get(i);
			if(names.contains(name)) return names;
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
