package de.westnordost.streetcomplete.quests;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.view.Item;

public class LastPickedValuesStore
{
	private final SharedPreferences prefs;

	@Inject public LastPickedValuesStore(SharedPreferences prefs)
	{
		this.prefs = prefs;
	}

	public void addLastPicked(String key, Iterable<String> newValues)
	{
		LinkedList<String> values = load(key);
		for (String value : newValues)
		{
			values.remove(value);
			values.addFirst(value);
		}
		prefs.edit().putString(getKey(key), TextUtils.join(",",values)).apply();
	}

	public void addLastPicked(String key, String value)
	{
		addLastPicked(key, Collections.singleton(value));
	}

	public void moveLastPickedToFront(String key, LinkedList<Item> items, List<Item> itemPool)
	{
		LinkedList<Item> lastPickedItems = findItems(load(key), itemPool);
		Iterator<Item> reverseIt = lastPickedItems.descendingIterator();
		while(reverseIt.hasNext())
		{
			Item lastPicked = reverseIt.next();
			if(!items.remove(lastPicked)) items.removeLast();
			items.addFirst(lastPicked);
		}
	}

	private LinkedList<String> load(String key)
	{
		LinkedList<String> result = new LinkedList<>();
		String values = prefs.getString(getKey(key), null);
		if (values != null) result.addAll(Arrays.asList(values.split(",")));
		return result;
	}

	private String getKey(String key)
	{
		return Prefs.IMAGE_LIST_LAST_PICKED_PREFIX + key;
	}

	private static LinkedList<Item> findItems(List<String> values, Iterable<Item> itemPool)
	{
		LinkedList<Item> result = new LinkedList<>();
		for (String value : values)
		{
			Item item = findItem(value, itemPool);
			if(item != null)
			{
				result.add(item);
			}
		}
		return result;
	}

	private static Item findItem(String value, Iterable<Item> itemPool)
	{
		for (Item item : itemPool)
		{
			if(item.isGroup())
			{
				Item subitem = findItem(value, item.getItems());
				if(subitem != null) return subitem;
			}
			// returns only items which are not groups themselves
			else if(value.equals(item.value))
			{
				return item;
			}
		}
		return null;
	}
}
