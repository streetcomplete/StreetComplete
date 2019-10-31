package de.westnordost.streetcomplete.data.osm;

import de.westnordost.osmapi.map.data.Element;

public class ElementKey
{
	private Element.Type elementType;
	private long elementId;

	public ElementKey(Element.Type elementType, long elementId)
	{
		this.elementType = elementType;
		this.elementId = elementId;
	}

	public Element.Type getElementType()
	{
		return elementType;
	}

	public long getElementId()
	{
		return elementId;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || !(o instanceof ElementKey)) return false;

		ElementKey that = (ElementKey) o;

		return
				elementType == that.elementType &&
				elementId == that.elementId;
	}

	@Override public int hashCode()
	{
		return (int) ((elementType.ordinal() * 31) + elementId);
	}
}
