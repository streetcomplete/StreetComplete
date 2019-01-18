package de.westnordost.streetcomplete.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class MultiIterableTest
{
	@Test public void emptyList()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Collections.emptyList());
		assertEquals("",concat(itb));
	}

	@Test public void oneList()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Arrays.asList("a","b","c"));
		assertEquals("a b c",concat(itb));
	}

	@Test public void listAllowsNulls()
	{
		MultiIterable<String> itb = new MultiIterable<>();
		itb.add(Arrays.asList("a",null,"c"));
		assertEquals("a null c",concat(itb));
	}

	@Test public void multipleLists()
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
