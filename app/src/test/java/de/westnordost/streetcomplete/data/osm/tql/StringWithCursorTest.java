package de.westnordost.streetcomplete.data.osm.tql;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class StringWithCursorTest
{
	@Test public void advance()
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
		} catch (IndexOutOfBoundsException ignore) {  }
	}

	@Test public void advanceBy()
	{
		StringWithCursor x = new StringWithCursor("wundertuete",Locale.US);
		assertEquals("wunder",x.advanceBy(6));
		assertEquals("", x.advanceBy(0));
		try
		{
			x.advanceBy(-1);
			fail();
		} catch(IndexOutOfBoundsException ignore) { }
		assertEquals("tuete",x.advanceBy(99999));
	}

	@Test public void nextIsAndAdvance()
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

	@Test public void nextIsAndAdvanceChar()
	{
		StringWithCursor x = new StringWithCursor("test123",Locale.US);
		assertTrue(x.nextIsAndAdvance('t'));
		assertEquals(1, x.getCursorPos());
		assertFalse(x.nextIsAndAdvance('t'));
		x.advanceBy(3);
		assertTrue(x.nextIsAndAdvance('1'));
		assertEquals(5, x.getCursorPos());
	}

	@Test public void nextIsAndAdvanceIgnoreCase()
	{
		StringWithCursor x = new StringWithCursor("test123",Locale.US);
		assertTrue(x.nextIsAndAdvanceIgnoreCase("TE"));
		assertTrue(x.nextIsAndAdvanceIgnoreCase("st"));
	}

	@Test public void findNext()
	{
		StringWithCursor x = new StringWithCursor("abc abc",Locale.US);
		assertEquals("abc abc".length(), x.findNext("wurst"));

		assertEquals(0,x.findNext("abc"));
		x.advance();
		assertEquals(3, x.findNext("abc"));
	}

	@Test public void findNextChar()
	{
		StringWithCursor x = new StringWithCursor("abc abc",Locale.US);
		assertEquals("abc abc".length(), x.findNext('x'));

		assertEquals(0,x.findNext('a'));
		x.advance();
		assertEquals(3,x.findNext('a'));
	}

	@Test public void nextIsChar()
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

	@Test public void nextIsString()
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

	@Test public void nextIsStringIgnoreCase()
	{
		StringWithCursor x = new StringWithCursor("abc",Locale.US);
		assertTrue(x.nextIsIgnoreCase("A"));
		assertTrue(x.nextIsIgnoreCase("a"));
	}
}
