package de.westnordost.streetcomplete.data.osm.changes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** A diff that can be applied on a map of strings. Use StringMapChangesBuilder to conveniently build
 *  it. A StringMapChanges is immutable. */
public class StringMapChanges
{
	private final List<StringMapEntryChange> changes;

	public List<StringMapEntryChange> getChanges()
	{
		return Collections.unmodifiableList(changes);
	}

	public StringMapChanges(@NonNull List<StringMapEntryChange> changes)
	{
		this.changes = changes;
	}

	/** @return a StringMapChanges that exactly reverses this StringMapChanges */
	public StringMapChanges reversed()
	{
		List<StringMapEntryChange> reverse = new ArrayList<>(changes.size());
		for(StringMapEntryChange change : changes)
		{
			StringMapEntryChange reverseChange = change.reversed();
			reverse.add(reverseChange);
		}
		return new StringMapChanges(reverse);
	}

	public boolean hasConflictsTo(@NonNull final Map<String,String> map)
	{
		return getConflictsTo(map).iterator().hasNext();
	}

	/** Return an iterable to iterate through the changes that have conflicts with the given map */
	public Iterable<StringMapEntryChange> getConflictsTo(@NonNull final Map<String,String> map)
	{
		return () -> new ConflictIterator(map);
	}

	/** Applies this diff to the given map. */
	public void applyTo(@NonNull Map<String,String> map)
	{
		if(hasConflictsTo(map))
		{
			throw new IllegalStateException("Could not apply the diff, there is at least one conflict.");
		}

		for(StringMapEntryChange change : changes)
		{
			change.applyTo(map);
		}
	}

	public boolean isEmpty()
	{
		return changes.isEmpty();
	}

	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		for(StringMapEntryChange change : changes)
		{
			if(first)
			{
				first = false;
			}
			else
			{
				sb.append(", ");
			}

			sb.append(change.toString());
		}

		return sb.toString();
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof StringMapChanges)) return false;
		StringMapChanges o = (StringMapChanges) other;
		return changes.equals(o.changes);
	}

	@Override public int hashCode()
	{
		return changes.hashCode();
	}

	private class ConflictIterator implements Iterator<StringMapEntryChange>
	{
		private Map<String,String> map;
		private StringMapEntryChange next;
		private Iterator<StringMapEntryChange> it = changes.iterator();

		public ConflictIterator(@NonNull Map<String,String> map)
		{
			this.map = map;
		}

		@Override public boolean hasNext()
		{
			findNext();
			return next != null;
		}

		@Override public StringMapEntryChange next()
		{
			findNext();
			StringMapEntryChange result = next;
			next = null;
			if(result == null)
			{
				throw new NoSuchElementException();
			}
			return result;
		}

		private void findNext()
		{
			if(next == null)
			{
				while (it.hasNext())
				{
					StringMapEntryChange change = it.next();
					if (change.conflictsWith(map))
					{
						next = change;
						return;
					}
				}
			}
		}

		@Override public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
