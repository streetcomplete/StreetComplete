package de.westnordost.streetcomplete.data.osm.changes;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringMapChangesBuilderTest extends TestCase
{
	public void testDelete()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.delete("exists");
		List<StringMapEntryChange> changes = builder.create().getChanges();

		assertEquals(1, changes.size());
		assertEquals(StringMapEntryDelete.class, changes.get(0).getClass());
		StringMapEntryDelete change = (StringMapEntryDelete) changes.get(0);
		assertEquals("exists", change.key);
		assertEquals("like this", change.valueBefore);
	}

	public void testDeleteNonExistingFails()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		try
		{
			builder.delete("does not exist");
		}
		catch(IllegalArgumentException e) {	return;	}
		fail();
	}

	public void testDeleteIfExistsNonExistingDoesNotFail()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.deleteIfExists("does not exist");
	}

	public void testAdd()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.add("does not exist", "but now");
		List<StringMapEntryChange> changes = builder.create().getChanges();

		assertEquals(1, changes.size());
		assertEquals(StringMapEntryAdd.class, changes.get(0).getClass());
		StringMapEntryAdd change = (StringMapEntryAdd) changes.get(0);
		assertEquals("does not exist", change.key);
		assertEquals("but now", change.value);
	}

	public void testAddAlreadyExistingFails()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		try
		{
			builder.add("exists", "like that");
		}
		catch(IllegalArgumentException e) {	return;	}
		fail();
	}

	public void testModify()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.modify("exists", "like that");
		List<StringMapEntryChange> changes = builder.create().getChanges();

		assertEquals(1, changes.size());
		assertEquals(StringMapEntryModify.class, changes.get(0).getClass());
		StringMapEntryModify change = (StringMapEntryModify) changes.get(0);

		assertEquals("exists", change.key);
		assertEquals("like this", change.valueBefore);
		assertEquals("like that", change.value);
	}

	public void testModifyIfExistsNonExistingDoesNotFail()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.modifyIfExists("does not exist","bla");
	}

	public void testDuplicateChange()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.modify("exists", "like that");

		try
		{
			builder.delete("exists");
			fail();
		}
		catch(IllegalStateException e) { }
	}

	private Map<String,String> createMap()
	{
		Map<String,String> result = new HashMap<>();
		result.put("exists","like this");
		return result;
	}
}
