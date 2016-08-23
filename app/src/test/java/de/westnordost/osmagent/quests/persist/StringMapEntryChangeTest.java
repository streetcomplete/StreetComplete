package de.westnordost.osmagent.quests.persist;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmagent.quests.StringMapEntryChange;

public class StringMapEntryChangeTest extends TestCase
{
	public void testAdd()
	{
		StringMapEntryChange c = new StringMapEntryChange("a","b", StringMapEntryChange.Type.ADD);
		Map<String,String> m = new HashMap<>();

		assertEquals("ADD a = b",c.toString());

		assertFalse(c.conflictsWith(m));

		c.applyTo(m);
		assertEquals("b", m.get("a"));

		assertTrue(c.conflictsWith(m));
	}

	public void testDelete()
	{
		StringMapEntryChange c = new StringMapEntryChange("a","b", StringMapEntryChange.Type.DELETE);
		Map<String,String> m = new HashMap<>();
		m.put("a","c");

		assertEquals("DELETE a = b",c.toString());

		assertTrue(c.conflictsWith(m));
		m.put("a","b");
		assertFalse(c.conflictsWith(m));

		c.applyTo(m);
		assertFalse(m.containsKey("a"));
		assertTrue(c.conflictsWith(m));
	}
}
