package de.westnordost.streetcomplete.util;

import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/** Iterate through several iterables of type T with just one iterator! */
public class MultiIterable<T> implements Iterable<T>
{
	private final Queue<Iterable<T>> queue;

	public MultiIterable()
	{
		queue = new ArrayDeque<>();
	}

	@NonNull @Override public Iterator<T> iterator()
	{
		return new MultiIterator<>(queue.iterator());
	}

	public void add(Iterable<T> iterable)
	{
		queue.add(iterable);
	}

	public static class MultiIterator<T> implements Iterator<T>
	{
		private final Iterator<Iterable<T>> it;
		private Iterator<T> currentIt;
		private T next;
		private boolean nextValid;

		private MultiIterator(Iterator<Iterable<T>> it)
		{
			this.it = it;
		}

		@Override public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override public boolean hasNext()
		{
			if (!nextValid) nextValid = moveToNext();
			return nextValid;
		}

		@Override public T next()
		{
			if (!hasNext()) throw new NoSuchElementException();
			nextValid = false;
			return next;
		}

		private boolean moveToNext()
		{
			while(currentIt != null || it.hasNext())
			{
				if (currentIt == null)
				{
					currentIt = it.next().iterator();
				}
				else if (!currentIt.hasNext())
				{
					currentIt = null;
				}
				else
				{
					next = currentIt.next();
					return true;
				}
			}
			return false;
		}
	}
}


