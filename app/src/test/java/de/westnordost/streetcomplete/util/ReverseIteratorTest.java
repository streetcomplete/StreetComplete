package de.westnordost.streetcomplete.util;

import junit.framework.TestCase;

import java.util.Arrays;

public class ReverseIteratorTest extends TestCase
{
	public void testReverse()
	{
		ReverseIterator<String> it = new ReverseIterator<>(Arrays.asList("a", "b", "c"));
		assertEquals("c", it.next());
		assertEquals("b", it.next());
		assertEquals("a", it.next());
		assertFalse(it.hasNext());
	}
}
