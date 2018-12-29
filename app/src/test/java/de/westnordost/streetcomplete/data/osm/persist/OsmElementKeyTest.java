package de.westnordost.streetcomplete.data.osm.persist;

import org.junit.Test;

import de.westnordost.osmapi.map.data.Element;

import static org.junit.Assert.*;

public class OsmElementKeyTest
{
	@Test public void almostEqual()
	{
		OsmElementKey
			a = new OsmElementKey(Element.Type.NODE, 1),
			b = new OsmElementKey(Element.Type.WAY, 1),
			c = new OsmElementKey(Element.Type.NODE, 2);

		assertNotEquals(a, b);
		assertNotEquals(a, c);

		assertNotEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a.hashCode(), c.hashCode());
	}

	@Test public void equal()
	{
		OsmElementKey
				a = new OsmElementKey(Element.Type.NODE, 1),
				b = new OsmElementKey(Element.Type.NODE, 1);

		assertEquals(a,b);
		assertEquals(a.hashCode(), b.hashCode());
	}
}
