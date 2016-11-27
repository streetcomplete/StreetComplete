package de.westnordost.streetcomplete.data.osm.persist;

import junit.framework.TestCase;

import de.westnordost.osmapi.map.data.Element;

public class OsmElementKeyTest extends TestCase
{
	public void testAlmostEqual()
	{
		OsmElementKey
			a = new OsmElementKey(Element.Type.NODE, 1),
			b = new OsmElementKey(Element.Type.WAY, 1),
			c = new OsmElementKey(Element.Type.NODE, 2);

		assertFalse(a.equals(b));
		assertFalse(a.equals(c));

		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.hashCode() == c.hashCode());
	}

	public void testEqual()
	{
		OsmElementKey
				a = new OsmElementKey(Element.Type.NODE, 1),
				b = new OsmElementKey(Element.Type.NODE, 1);

		assertEquals(a,b);
		assertEquals(a.hashCode(), b.hashCode());
	}
}
