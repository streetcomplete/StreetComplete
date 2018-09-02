package de.westnordost.streetcomplete.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

public class FlattenIterableTest extends TestCase
{
	public void testEmptyList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Collections.emptyList());
		assertEquals("",concat(itb));
	}

	public void testAlreadyFlatList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a","b","c"));
		assertEquals("a b c",concat(itb));
	}

	public void testListAllowsNulls()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a",null,"c"));
		assertEquals("a null c",concat(itb));
	}

	public void testListWithDifferentTypesFails()
	{
		try
		{
			FlattenIterable<String> itb = new FlattenIterable<>(String.class);
			itb.add(Arrays.asList("a", 4));
			concat(itb);
			fail();
		} catch (IllegalArgumentException ignore) {}
	}

	public void testNestedList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a",Arrays.asList("b", "c"),"d"));
		assertEquals("a b c d",concat(itb));
	}

	public void testDeeperNestedList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a",Arrays.asList("b", Arrays.asList("c", "d")),"e"));
		assertEquals("a b c d e",concat(itb));
	}

	public void testMultipleLists()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a","b",Arrays.asList("c","d")));
		itb.add(Arrays.asList("e","f"));
		assertEquals("a b c d e f",concat(itb));
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
