package de.westnordost.streetcomplete.data.osm.tql;

import java.util.Locale;

/** Convenience class to make it easier to go step by step through a string */
public class StringWithCursor
{
	private final String string;
	private final Locale locale;
	private int cursor = 0;

	public StringWithCursor(String string, Locale locale)
	{
		this.string = string;
		this.locale = locale;
	}

	/** Advances the cursor if str is the next thing at the cursor
	 *  @return whether the next string was the str */
	public boolean nextIsAndAdvance(String str)
	{
		if(!nextIs(str)) return false;
		advanceBy(str.length());
		return true;
	}

	public boolean nextIsAndAdvance(char c)
	{
		if(!nextIs(c)) return false;
		advance();
		return true;
	}

	/** Advances the cursor if str or str.toUpperCase() is the next thing at the cursor
	 *  @return whether the next string was the str or str.toUpperCase */
	public boolean nextIsAndAdvanceIgnoreCase(String str)
	{
		if(!nextIsIgnoreCase(str)) return false;
		advanceBy(str.length());
		return true;
	}

	/** @return whether the cursor reached the end */
	public boolean isAtEnd()      { return cursor >= string.length(); }
	public boolean isAtEnd(int x) { return cursor + x >= string.length(); }

	public int findNext(String str) { return toDelta(string.indexOf(str, cursor)); }
	public int findNext(char c)	    { return findNext(c, 0); }

	public int findNext(char c, int offs) { return toDelta(string.indexOf(c, cursor + offs)); }

	public int getCursorPos() { return cursor; }

	/** Advance cursor by one and return the character that was at that position
	 *  @throws IndexOutOfBoundsException if cursor is already at the end */
	public char advance()
	{
		char result = string.charAt(cursor);
		cursor = Math.min(string.length(), cursor + 1);
		return result;
	}

	/** Advance cursor by x and return the string that inbetween the two positions.
	 *  If cursor+x is beyond the end of the string, the method will just return the string until
	 *  the end of the string
	 *  @throws IndexOutOfBoundsException if x < 0 */
	public String advanceBy(int x)
	{
		int end = cursor + x;
		String result;
		if(string.length() < end)
		{
			result = string.substring(cursor);
			cursor = string.length();
		}
		else
		{
			result = string.substring(cursor, end);
			cursor = end;
		}
		return result;
	}

	private Character getChar()
	{
		if(cursor >= string.length()) return null;
		return string.charAt(cursor);
	}

	public boolean previousIs(char c)
	{
		return c == string.charAt(cursor-1);
	}

	public boolean nextIs(char c)
	{
		return getChar() != null && c == getChar();
	}

	public boolean nextIs(String str)
	{
		return string.startsWith(str, cursor);
	}

	public boolean nextIsIgnoreCase(String str)
	{
		return nextIs(str.toLowerCase(locale)) || nextIs(str.toUpperCase(locale));
	}

	private int toDelta(int index)
	{
		if(index == -1) return string.length() - cursor;
		return index - cursor;
	}

	// good for debugging
	@Override
	public String toString()
	{
		return string.substring(0, cursor) + "►" + string.substring(cursor);
	}
}
