package de.westnordost.osmagent.data.osm.changes;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class StringMapEntryDeleteTest extends TestCase
{
	public void testDelete()
	{
		StringMapEntryDelete c = new StringMapEntryDelete("a","b");
		Map<String,String> m = new HashMap<>();
		m.put("a","c");

		assertEquals("DELETE \"a\"=\"b\"",c.toString());

		assertTrue(c.conflictsWith(m));
		m.put("a","b");
		assertFalse(c.conflictsWith(m));

		c.applyTo(m);
		assertFalse(m.containsKey("a"));
		assertTrue(c.conflictsWith(m));
	}
}
