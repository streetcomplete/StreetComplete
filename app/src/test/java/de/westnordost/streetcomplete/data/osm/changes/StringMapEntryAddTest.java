package de.westnordost.streetcomplete.data.osm.changes;



import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StringMapEntryAddTest
{
	@Test public void add()
	{
		StringMapEntryAdd c = new StringMapEntryAdd("a","b");
		Map<String,String> m = new HashMap<>();

		assertEquals("ADD \"a\"=\"b\"",c.toString());

		assertFalse(c.conflictsWith(m));

		c.applyTo(m);
		assertEquals("b", m.get("a"));

		assertTrue(c.conflictsWith(m));
	}

	@Test public void reverse()
	{
		Map<String,String> m = new HashMap<>();

		StringMapEntryChange add = new StringMapEntryAdd("a","b");
		StringMapEntryChange reverseAdd = add.reversed();

		add.applyTo(m);
		reverseAdd.applyTo(m);

		assertTrue(m.isEmpty());
	}
}
