package de.westnordost.streetcomplete.view;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

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
}
