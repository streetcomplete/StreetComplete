package de.westnordost.streetcomplete.data.osm.persist;

import org.junit.Test;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.osm.ElementKey;

import static org.junit.Assert.*;

public class ElementKeyTest
{
	@Test public void almostEqual()
	{
		ElementKey
			a = new ElementKey(Element.Type.NODE, 1),
			b = new ElementKey(Element.Type.WAY, 1),
			c = new ElementKey(Element.Type.NODE, 2);

		assertNotEquals(a, b);
		assertNotEquals(a, c);

		assertNotEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a.hashCode(), c.hashCode());
	}

	@Test public void equal()
	{
		ElementKey
				a = new ElementKey(Element.Type.NODE, 1),
				b = new ElementKey(Element.Type.NODE, 1);

		assertEquals(a,b);
		assertEquals(a.hashCode(), b.hashCode());
	}
}
