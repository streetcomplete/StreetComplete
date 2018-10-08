package de.westnordost.streetcomplete.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Item
{
	public final String value;
	public final int titleId;
	public final int descriptionId;
	public final int drawableId;

	final Item[] items;

	public Item(String value, int drawableId)
	{
		this(value, drawableId, 0, 0, null);
	}

	public Item(String value, int drawableId, int titleId)
	{
		this(value, drawableId, titleId, 0, null);
	}

	public Item(String value, int drawableId, int titleId, int descriptionId)
	{
		this(value, drawableId, titleId, descriptionId, null);
	}

	public Item(String value, int drawableId, int titleId, Item[] items)
	{
		this(value, drawableId, titleId, 0, items);
	}

	public Item(Item copy, Item[] children)
	{
		this(copy.value, copy.drawableId, copy.titleId, copy.descriptionId, children);
	}

	public Item(String value, int drawableId, int titleId, int descriptionId, Item[] items)
	{
		this.value = value;
		this.items = items;
		this.titleId = titleId;
		this.descriptionId = descriptionId;
		this.drawableId = drawableId;
	}

	public boolean hasValue() { return value != null; }
	public boolean isGroup() { return items != null; }

	public List<Item> getItems()
	{
		return new ArrayList<>(Arrays.asList(items));
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Item item = (Item) o;

		if (titleId != item.titleId) return false;
		if (descriptionId != item.descriptionId) return false;
		if (drawableId != item.drawableId) return false;
		if (value != null ? !value.equals(item.value) : item.value != null) return false;
		return Arrays.equals(items, item.items);
	}

	@Override public int hashCode()
	{
		int result = value != null ? value.hashCode() : 0;
		result = 31 * result + titleId;
		result = 31 * result + descriptionId;
		result = 31 * result + drawableId;
		result = 31 * result + Arrays.hashCode(items);
		return result;
	}
}
