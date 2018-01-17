package de.westnordost.streetcomplete.view;

public class Item
{
	public final String value;
	public final int titleId;
	public final int drawableId;

	final Item[] items;

	public Item(String value, int drawableId)
	{
		this(value, drawableId, -1, null);
	}

	public Item(String value, int drawableId, int titleId)
	{
		this(value, drawableId, titleId, null);
	}

	public Item(String value, int drawableId, int titleId, Item[] items)
	{
		this.value = value;
		this.items = items;
		this.titleId = titleId;
		this.drawableId = drawableId;
	}

	public boolean isGroup() { return items != null; }
}