package de.westnordost.streetcomplete.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

public class MultiIterableTest extends TestCase
{
	public void testEmptyList()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Collections.emptyList());
		assertEquals("",concat(itb));
	}

	public void testOneList()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Arrays.asList("a","b","c"));
		assertEquals("a b c",concat(itb));
	}

	public void testListAllowsNulls()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Arrays.asList("a",null,"c"));
		assertEquals("a null c",concat(itb));
	}

	public void testMultipleLists()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Arrays.asList("a","b"));
		itb.add(Arrays.asList("c","d"));
		assertEquals("a b c d",concat(itb));
	}

	private static String concat(Iterable<String> it)
	{
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String s : it)
		{
			if(first) first = false;
			else b.append(" ");
			b.append(s);
		}
		return b.toString();
	}
}
