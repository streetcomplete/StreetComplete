package de.westnordost.streetcomplete.data.osm.changes;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StringMapEntryModifyTest
{
	@Test public void modify()
	{
		StringMapEntryModify c = new StringMapEntryModify("a","b","c");
		Map<String,String> m = new HashMap<>();
		m.put("a","b");

		assertEquals("MODIFY \"a\"=\"b\" -> \"a\"=\"c\"",c.toString());

		assertFalse(c.conflictsWith(m));
		c.applyTo(m);
		assertTrue(c.conflictsWith(m));
	}

	@Test public void reverse()
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
