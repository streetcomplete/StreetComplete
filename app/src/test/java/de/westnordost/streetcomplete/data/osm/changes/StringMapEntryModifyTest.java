package de.westnordost.streetcomplete.data.osm.changes;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class StringMapEntryModifyTest extends TestCase
{
	public void testModify()
	{
		StringMapEntryModify c = new StringMapEntryModify("a","b","c");
		Map<String,String> m = new HashMap<>();
		m.put("a","b");

		assertEquals("MODIFY \"a\"=\"b\" -> \"a\"=\"c\"",c.toString());

		assertFalse(c.conflictsWith(m));
		c.applyTo(m);
		assertTrue(c.conflictsWith(m));
	}

	public void testReverse()
	{
		StringMapEntryChange modify = new StringMapEntryModify("a","b","c");
		StringMapEntryChange reverseModify = modify.reversed();

		Map<String,String> m = new HashMap<>();
		m.put("a","b");

		modify.applyTo(m);
		reverseModify.applyTo(m);

		assertEquals(1, m.size());
		assertEquals("b",m.get("a"));
	}
}
