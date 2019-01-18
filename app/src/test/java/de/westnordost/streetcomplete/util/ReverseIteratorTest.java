package de.westnordost.streetcomplete.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ReverseIteratorTest
{
	@Test public void reverse()
	{
		ReverseIterator<String> it = new ReverseIterator<>(Arrays.asList("a", "b", "c"));
		assertEquals("c", it.next());
		assertEquals("b", it.next());
		assertEquals("a", it.next());
		assertFalse(it.hasNext());
	}
}
