package de.westnordost.streetcomplete.view;

public class Item
{
	public final String value;
	public final int titleId;
	public final int descriptionId;
	public final int drawableId;

	final Item[] items;

	public Item(String value, int drawableId)
	{
		this(value, drawableId, -1, -1, null);
	}

	public Item(String value, int drawableId, int titleId)
	{
		this(value, drawableId, titleId, -1, null);
	}

	public Item(String value, int drawableId, int titleId, int descriptionId)
	{
		this(value, drawableId, titleId, descriptionId, null);
	}

	public Item(String value, int drawableId, int titleId, Item[] items)
	{
		this(value, drawableId, titleId, -1, items);
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
