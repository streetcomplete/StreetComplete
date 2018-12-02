package de.westnordost.streetcomplete.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReverseIterator<T> implements Iterator<T>
{
	private final ListIterator<T> iterator;

	public ReverseIterator(List<T> list) { this.iterator = list.listIterator(list.size()); }
	@Override public boolean hasNext() { return iterator.hasPrevious(); }
	@Override public T next() { return iterator.previous(); }
	@Override public void remove() { iterator.remove(); }
}
