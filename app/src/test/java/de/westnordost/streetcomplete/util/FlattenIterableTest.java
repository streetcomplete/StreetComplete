package de.westnordost.streetcomplete.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FlattenIterableTest
{
	@Test public void emptyList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Collections.emptyList());
		assertEquals("",concat(itb));
	}

	@Test public void alreadyFlatList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a","b","c"));
		assertEquals("a b c",concat(itb));
	}

	@Test public void listAllowsNulls()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a",null,"c"));
		assertEquals("a null c",concat(itb));
	}

	@Test(expected = IllegalArgumentException.class) public void listWithDifferentTypesFails()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a", 4));
		concat(itb);
	}

	@Test public void nestedList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a",Arrays.asList("b", "c"),"d"));
		assertEquals("a b c d",concat(itb));
	}

	@Test public void deeperNestedList()
	{
		FlattenIterable<String> itb = new FlattenIterable<>(String.class);
		itb.add(Arrays.asList("a",Arrays.asList("b", Arrays.asList("c", "d")),"e"));
		assertEquals("a b c d e",concat(itb));
	}

	@Test public void multipleLists()
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
