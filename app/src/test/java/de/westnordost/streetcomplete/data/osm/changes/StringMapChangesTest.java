package de.westnordost.streetcomplete.data.osm.changes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StringMapChangesTest
{
	@Test public void empty()
	{
		List<StringMapEntryChange> list = Collections.emptyList();

		StringMapChanges changes = new StringMapChanges(list);
		assertEquals("", changes.toString());
		assertTrue(changes.getChanges().isEmpty());

		// executable without error:
		Map<String,String> someMap = Collections.emptyMap();
		changes.applyTo(someMap);

		assertFalse(changes.hasConflictsTo(someMap));
	}

	@Test public void one()
	{
		StringMapEntryChange change1 = mock(StringMapEntryChange.class);
		when(change1.toString()).thenReturn("a");

		StringMapChanges changes = new StringMapChanges(Collections.singletonList(change1));
		Map<String,String> someMap = Collections.emptyMap();

		assertEquals("a",changes.toString());

		changes.applyTo(someMap);
		verify(change1).applyTo(someMap);

		changes.hasConflictsTo(someMap);
		verify(change1, atLeastOnce()).conflictsWith(someMap);
	}

	@Test public void two()
	{
		List<StringMapEntryChange> list = new ArrayList<>();

		StringMapEntryChange change1 = mock(StringMapEntryChange.class);
		when(change1.toString()).thenReturn("a");
		list.add(change1);
		StringMapEntryChange change2 = mock(StringMapEntryChange.class);
		when(change2.toString()).thenReturn("b");
		list.add(change2);

		StringMapChanges changes = new StringMapChanges(list);
		Map<String,String> someMap = Collections.emptyMap();

		assertEquals("a, b",changes.toString());

		changes.applyTo(someMap);
		verify(change1).applyTo(someMap);
		verify(change2).applyTo(someMap);

		changes.hasConflictsTo(someMap);
		verify(change1, atLeastOnce()).conflictsWith(someMap);
		verify(change2, atLeastOnce()).conflictsWith(someMap);
	}

	@Test(expected = IllegalStateException.class) public void applyToConflictFails()
	{
		Map<String,String> someMap = Collections.emptyMap();

		StringMapEntryChange change1 = mock(StringMapEntryChange.class);
		when(change1.conflictsWith(someMap)).thenReturn(true);

		StringMapChanges changes = new StringMapChanges(Collections.singletonList(change1));

		changes.applyTo(someMap);
	}

	@Test public void getConflicts()
	{
		List<StringMapEntryChange> list = new ArrayList<>();
		Map<String,String> someMap = Collections.emptyMap();

		StringMapEntryChange conflict = mock(StringMapEntryChange.class);
		when(conflict.conflictsWith(someMap)).thenReturn(true);

		list.add( mock(StringMapEntryChange.class));
		list.add( mock(StringMapEntryChange.class));
		list.add( conflict );
		list.add( mock(StringMapEntryChange.class));
		list.add( conflict );

		StringMapChanges changes = new StringMapChanges(list);

		changes.getConflictsTo(someMap);

		Iterator<StringMapEntryChange> it = changes.getConflictsTo(someMap).iterator();

		assertSame(conflict, it.next());
		assertSame(conflict, it.next());
	}
}
