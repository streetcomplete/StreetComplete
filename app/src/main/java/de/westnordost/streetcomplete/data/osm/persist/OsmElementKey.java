package de.westnordost.streetcomplete.data.osm.persist;

import de.westnordost.osmapi.map.data.Element;

public class OsmElementKey
{
	private Element.Type elementType;
	private long elementId;

	public OsmElementKey(Element.Type elementType, long elementId)
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
		if (o == null || !(o instanceof OsmElementKey)) return false;

		OsmElementKey that = (OsmElementKey) o;

		return
				elementType == that.elementType &&
				elementId == that.elementId;
	}

	@Override public int hashCode()
	{
		return (int) ((elementType.ordinal() * 31) + elementId);
	}
}
