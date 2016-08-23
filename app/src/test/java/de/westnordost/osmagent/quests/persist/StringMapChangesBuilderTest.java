package de.westnordost.osmagent.quests.persist;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmagent.quests.StringMapDiffBuilder;
import de.westnordost.osmagent.quests.StringMapEntryChange;

public class StringMapChangesBuilderTest extends TestCase
{
	public void testDelete()
	{
		StringMapDiffBuilder builder = new StringMapDiffBuilder(createMap());
		builder.delete("exists");
		List<StringMapEntryChange> changes = builder.create().getChanges();

		assertEquals(1, changes.size());
		assertEquals(StringMapEntryChange.Type.DELETE, changes.get(0).type);
		assertEquals("exists", changes.get(0).key);
		assertEquals("like this", changes.get(0).value);
	}

	public void testDeleteNonExistingFails()
	{
		StringMapDiffBuilder builder = new StringMapDiffBuilder(createMap());
		try
		{
			builder.delete("does not exist");
		}
		catch(IllegalArgumentException e) {	return;	}
		fail();
	}

	public void testAdd()
	{
		StringMapDiffBuilder builder = new StringMapDiffBuilder(createMap());
		builder.add("does not exist", "but now");
		List<StringMapEntryChange> changes = builder.create().getChanges();

		assertEquals(1, changes.size());
		assertEquals(StringMapEntryChange.Type.ADD, changes.get(0).type);
		assertEquals("does not exist", changes.get(0).key);
		assertEquals("but now", changes.get(0).value);
	}

	public void testAddAlreadyExistingFails()
	{
		StringMapDiffBuilder builder = new StringMapDiffBuilder(createMap());
		try
		{
			builder.add("exists", "like that");
		}
		catch(IllegalArgumentException e) {	return;	}
		fail();
	}

	public void testModify()
	{
		StringMapDiffBuilder builder = new StringMapDiffBuilder(createMap());
		builder.modify("exists", "like that");
		List<StringMapEntryChange> changes = builder.create().getChanges();

		assertEquals(2, changes.size());
		assertEquals(StringMapEntryChange.Type.DELETE, changes.get(0).type);
		assertEquals("exists", changes.get(0).key);
		assertEquals("like this", changes.get(0).value);
		assertEquals(StringMapEntryChange.Type.ADD, changes.get(1).type);
		assertEquals("exists", changes.get(1).key);
		assertEquals("like that", changes.get(1).value);
	}

	private Map<String,String> createMap()
	{
		Map<String,String> result = new HashMap<>();
		result.put("exists","like this");
		return result;
	}
}
