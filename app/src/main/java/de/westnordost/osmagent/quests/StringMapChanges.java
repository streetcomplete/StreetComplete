package de.westnordost.osmagent.quests;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** A diff that can be applied on a map of strings. Use StringMapDiffBuilder to conveniently build
 *  it. A StringMapChanges is immutable. */
public class StringMapChanges implements Serializable
{
	static final long serialVersionUID = 1L;

	private List<StringMapEntryChange> changes;

	public List<StringMapEntryChange> getChanges()
	{
		return Collections.unmodifiableList(changes);
	}

	public StringMapChanges(@NonNull List<StringMapEntryChange> changes)
	{
		this.changes = changes;
	}

	public boolean hasConflictsTo(@NonNull final Map<String,String> map)
	{
		return getConflictsTo(map).iterator().hasNext();
	}

	/** Return an iterable to iterate through the changes that have conflicts with the given map */
	public Iterable<StringMapEntryChange> getConflictsTo(@NonNull final Map<String,String> map)
	{
		return new Iterable<StringMapEntryChange>()
		{
			@Override public Iterator<StringMapEntryChange> iterator()
			{
				return new ConflictIterator(map);
			}
		};
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

	@Override
	public String toString()
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
				sb.append("\n");
			}

			sb.append(change.toString());
		}

		return sb.toString();
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
			while(it.hasNext())
			{
				StringMapEntryChange change = it.next();
				if(change.conflictsWith(map))
				{
					next = change;
					return true;
				}
			}

			return next != null;
		}

		@Override public StringMapEntryChange next()
		{
			StringMapEntryChange result = next;
			next = null;
			if(result == null)
			{
				throw new NoSuchElementException();
			}
			return result;
		}

		@Override public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
