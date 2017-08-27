package de.westnordost.streetcomplete.data.osm.changes;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class StringMapEntryAddTest extends TestCase
{
	public void testAdd()
	{
		StringMapEntryAdd c = new StringMapEntryAdd("a","b");
		Map<String,String> m = new HashMap<>();

		assertEquals("ADD \"a\"=\"b\"",c.toString());

		assertFalse(c.conflictsWith(m));

		c.applyTo(m);
		assertEquals("b", m.get("a"));

		assertTrue(c.conflictsWith(m));
	}

	public void testReverse()
	{
		Map<String,String> m = new HashMap<>();

		StringMapEntryChange add = new StringMapEntryAdd("a","b");
		StringMapEntryChange reverseAdd = add.reversed();

		add.applyTo(m);
		reverseAdd.applyTo(m);

		assertTrue(m.isEmpty());
	}
}
