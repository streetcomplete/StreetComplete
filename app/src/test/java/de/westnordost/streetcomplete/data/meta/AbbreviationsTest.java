package de.westnordost.streetcomplete.data.meta;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class AbbreviationsTest extends TestCase
{
	public void testCapitalizeFirstLetter()
	{
		assertEquals("Straße", abbr("str: straße", Locale.GERMANY).getExpansion("str", true, true));
	}

	public void testWithAbbrDot()
	{
		assertEquals("Straße", abbr("str: straße", Locale.GERMANY).getExpansion("str.", true, true));
	}

	public void testIgnoreCase()
	{
		assertEquals("Straße", abbr("sTr: Straße", Locale.GERMANY).getExpansion("StR", true, true));
	}

	public void testExpectOwnWordByDefault()
	{
		assertEquals(null, abbr("st: street").getExpansion("Hanswurst", true, true));
	}

	public void testConcatenable()
	{
		assertEquals("Konigstraat", abbr("...str: Straat").getExpansion("Konigstr", true, true));
	}

	public void testConcatenableEnd()
	{
		assertEquals("Konigstraat", abbr("...str$: Straat").getExpansion("Konigstr", true, true));
	}

	public void testConcatenableWorksNormallyForNonConcatenation()
	{
		assertEquals("Straat", abbr("...str: Straat").getExpansion("str", true, true));
	}

	public void testOnlyFirstWord()
	{
		Abbreviations abbr = abbr("^st: Saint");
		assertEquals(null, abbr.getExpansion("st.", false, false));
		assertEquals("Saint", abbr.getExpansion("st.", true, false));
	}

	public void testOnlyLastWord()
	{
		Abbreviations abbr = abbr("str$: Straße");
		assertEquals(null, abbr.getExpansion("str", true, false));
		assertEquals(null, abbr.getExpansion("str", true, true));
		assertEquals("Straße", abbr.getExpansion("str", false, true));
	}

	public void testUnicode()
	{
		assertEquals("Блок", abbr("бл: Блок",new Locale("ru","RU")).getExpansion("бл", true, true));
	}

	public void testLocaleCase()
	{
		assertEquals("Блок", abbr("бл: блок",new Locale("ru","RU")).getExpansion("Бл", true, true));
	}

	public void testFindAbbreviation()
	{
		assertFalse(abbr("str: Straße", Locale.GERMANY).containsAbbreviations("stri stra straße"));
		assertTrue(abbr("str: Straße", Locale.GERMANY).containsAbbreviations("stri str straße"));
	}

	public void testThrowExceptionOnInvalidInput()
	{
		try
		{
			abbr("d:\n  - a\n  b: c\n");
			fail();
		}
		catch (RuntimeException e) { }
	}

	private Abbreviations abbr(String input)
	{
		return abbr(input, Locale.US);
	}

	private Abbreviations abbr(String input, Locale locale)
	{
		try
		{
			return new Abbreviations(new ByteArrayInputStream(input.getBytes("UTF-8")), locale);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
