package de.westnordost.streetcomplete.data.osm.changes;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class StringMapChangesBuilderTest
{
	@Test public void delete()
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

	@Test(expected = IllegalArgumentException.class) public void deleteNonExistingFails()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.delete("does not exist");
	}

	@Test public void deleteIfExistsNonExistingDoesNotFail()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.deleteIfExists("does not exist");
	}

	@Test public void add()
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

	@Test(expected = IllegalArgumentException.class) public void addAlreadyExistingFails()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.add("exists", "like that");
	}

	@Test public void modify()
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

	@Test public void modifyIfExistsNonExistingDoesNotFail()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.modifyIfExists("does not exist","bla");
	}

	@Test(expected = IllegalStateException.class) public void duplicateChangeFails()
	{
		StringMapChangesBuilder builder = new StringMapChangesBuilder(createMap());
		builder.modify("exists", "like that");

		builder.delete("exists");
	}

	private Map<String,String> createMap()
	{
		Map<String,String> result = new HashMap<>();
		result.put("exists","like this");
		return result;
	}
}
