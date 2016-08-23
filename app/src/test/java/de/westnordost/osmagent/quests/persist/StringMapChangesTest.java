package de.westnordost.osmagent.quests.persist;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmagent.quests.StringMapChanges;
import de.westnordost.osmagent.quests.StringMapDiffBuilder;
import de.westnordost.osmagent.quests.StringMapEntryChange;

public class StringMapChangesTest extends TestCase
{
	public void testToString()
	{
		String expect = "";

		StringMapDiffBuilder b = new StringMapDiffBuilder(createMap());
		b.add("a","b");
		expect += "ADD a = b";
		assertEquals(expect, b.create().toString());

		b.delete("exists");
		expect += "\nDELETE exists = like this";
		assertEquals(expect, b.create().toString());

		b.modify("also exists", "there");
		expect += "\nDELETE also exists = here\nADD also exists = there";
		assertEquals(expect, b.create().toString());
	}

	public void testApplyTo()
	{
		Map<String,String> m = createMap();

		createDiff(m).applyTo(m);

		assertEquals("b",m.get("a"));
		assertEquals("there", m.get("also exists"));
		assertEquals(2, m.size());
	}

	public void testApplyToConflict()
	{
		Map<String,String> m = createMap();
		StringMapChanges diff = createDiff(m);

		m.put("a","c");

		try
		{
			diff.applyTo(m);
			fail();
		}
		catch(IllegalStateException e) { }
	}

	public void testGetConflicts()
	{
		Map<String,String> m = createMap();
		StringMapChanges diff = createDiff(m);

		m.put("a","c");
		m.remove("exists");
		m.remove("also exists");

		List<StringMapEntryChange> conflicts = new ArrayList<>();
		for(StringMapEntryChange conflict : diff.getConflictsTo(m))
		{
			conflicts.add(conflict);
		}

		assertEquals(3, conflicts.size());

		assertEquals("ADD a = b", conflicts.get(0).toString());
		assertEquals("DELETE exists = like this", conflicts.get(1).toString());
		assertEquals("DELETE also exists = here", conflicts.get(2).toString());
	}

	private StringMapChanges createDiff(Map<String,String> m)
	{
		StringMapDiffBuilder b = new StringMapDiffBuilder(m);
		b.add("a","b");
		b.delete("exists");
		b.modify("also exists", "there");
		return b.create();
	}

	private Map<String,String> createMap()
	{
		Map<String,String> result = new HashMap<>();
		result.put("exists","like this");
		result.put("also exists","here");
		return result;
	}
}
