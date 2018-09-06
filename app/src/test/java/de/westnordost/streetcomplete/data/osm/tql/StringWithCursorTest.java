package de.westnordost.streetcomplete.data.osm.tql;

import junit.framework.TestCase;

import java.util.Locale;

public class StringWithCursorTest extends TestCase
{
	public void testAdvance()
	{
		StringWithCursor x = new StringWithCursor("ab", Locale.US);
		assertEquals(0, x.getCursorPos());
		assertEquals('a', x.advance());
		assertEquals(1, x.getCursorPos());
		assertEquals('b', x.advance());
		assertEquals(2, x.getCursorPos());

		try
		{
			x.advance();
			fail();
		} catch (IndexOutOfBoundsException e) {  }
	}

	public void testAdvanceBy()
	{
		StringWithCursor x = new StringWithCursor("wundertuete",Locale.US);
		assertEquals("wunder",x.advanceBy(6));
		assertEquals("", x.advanceBy(0));
		try
		{
			x.advanceBy(-1);
			fail();
		} catch(IndexOutOfBoundsException e) { }
		assertEquals("tuete",x.advanceBy(99999));
	}

	public void testNextIsAndAdvance()
	{
		StringWithCursor x = new StringWithCursor("test123",Locale.US);
		assertTrue(x.nextIsAndAdvance("te"));
		assertEquals(2, x.getCursorPos());
		assertFalse(x.nextIsAndAdvance("te"));
		x.advanceBy(3);
		assertTrue(x.nextIsAndAdvance("23"));
		assertEquals(7, x.getCursorPos());
		assertTrue(x.isAtEnd());
	}

	public void testNextIsAndAdvanceChar()
	{
		StringWithCursor x = new StringWithCursor("test123",Locale.US);
		assertTrue(x.nextIsAndAdvance('t'));
		assertEquals(1, x.getCursorPos());
		assertFalse(x.nextIsAndAdvance('t'));
		x.advanceBy(3);
		assertTrue(x.nextIsAndAdvance('1'));
		assertEquals(5, x.getCursorPos());
	}

	public void testNextIsAndAdvanceIgnoreCase()
	{
		StringWithCursor x = new StringWithCursor("test123",Locale.US);
		assertTrue(x.nextIsAndAdvanceIgnoreCase("TE"));
		assertTrue(x.nextIsAndAdvanceIgnoreCase("st"));
	}

	public void testFindNext()
	{
		StringWithCursor x = new StringWithCursor("abc abc",Locale.US);
		assertEquals("abc abc".length(), x.findNext("wurst"));

		assertEquals(0,x.findNext("abc"));
		x.advance();
		assertEquals(3, x.findNext("abc"));
	}

	public void testFindNextChar()
	{
		StringWithCursor x = new StringWithCursor("abc abc",Locale.US);
		assertEquals("abc abc".length(), x.findNext('x'));

		assertEquals(0,x.findNext('a'));
		x.advance();
		assertEquals(3,x.findNext('a'));
	}

	public void testNextIsChar()
	{
		StringWithCursor x = new StringWithCursor("abc",Locale.US);
		assertTrue(x.nextIs('a'));
		assertFalse(x.nextIs('b'));
		x.advance();
		assertTrue(x.nextIs('b'));
		x.advance();
		assertTrue(x.nextIs('c'));
		x.advance();
		assertFalse(x.nextIs('c'));
	}

	public void testNextIsString()
	{
		StringWithCursor x = new StringWithCursor("abc",Locale.US);
		assertTrue(x.nextIs("abc"));
		assertTrue(x.nextIs("ab"));
		assertFalse(x.nextIs("bc"));
		x.advance();
		assertTrue(x.nextIs("bc"));
		x.advance();
		x.advance();
		assertFalse(x.nextIs("c"));
	}

	public void testNextIsStringIgnoreCase()
	{
		StringWithCursor x = new StringWithCursor("abc",Locale.US);
		assertTrue(x.nextIsIgnoreCase("A"));
		assertTrue(x.nextIsIgnoreCase("a"));
	}
}
