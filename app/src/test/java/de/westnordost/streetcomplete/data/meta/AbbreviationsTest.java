package de.westnordost.streetcomplete.data.meta;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import static org.junit.Assert.*;

public class AbbreviationsTest
{
	@Test public void capitalizesFirstLetter() {
		assertEquals("Straße", abbr("str: straße", Locale.GERMANY).getExpansion("str", true, true));
	}

	@Test public void removesAbbreviationDot()
	{
		assertEquals("Straße", abbr("str: straße", Locale.GERMANY).getExpansion("str.", true, true));
	}

	@Test public void ignoresCase()
	{
		assertEquals("Straße", abbr("sTr: Straße", Locale.GERMANY).getExpansion("StR", true, true));
	}

	@Test public void expectsOwnWordByDefault()
	{
		assertNull(abbr("st: street").getExpansion("Hanswurst", true, true));
	}

	@Test public void concatenable()
	{
		assertEquals("Konigstraat", abbr("...str: Straat").getExpansion("Konigstr", true, true));
	}

	@Test public void concatenableEnd()
	{
		assertEquals("Konigstraat", abbr("...str$: Straat").getExpansion("Konigstr", true, true));
	}

	@Test public void concatenableWorksNormallyForNonConcatenation()
	{
		assertEquals("Straat", abbr("...str: Straat").getExpansion("str", true, true));
	}

	@Test public void onlyFirstWord()
	{
		Abbreviations abbr = abbr("^st: Saint");
		assertNull(abbr.getExpansion("st.", false, false));
		assertEquals("Saint", abbr.getExpansion("st.", true, false));
	}

	@Test public void onlyLastWord()
	{
		Abbreviations abbr = abbr("str$: Straße");
		assertNull(abbr.getExpansion("str", true, false));
		assertNull(abbr.getExpansion("str", true, true));
		assertEquals("Straße", abbr.getExpansion("str", false, true));
	}

	@Test public void unicode()
	{
		assertEquals("Блок", abbr("бл: Блок",new Locale("ru","RU")).getExpansion("бл", true, true));
	}

	@Test public void localeCase()
	{
		assertEquals("Блок", abbr("бл: блок",new Locale("ru","RU")).getExpansion("Бл", true, true));
	}

	@Test public void findsAbbreviation()
	{
		assertFalse(abbr("str: Straße", Locale.GERMANY).containsAbbreviations("stri stra straße"));
		assertTrue(abbr("str: Straße", Locale.GERMANY).containsAbbreviations("stri str straße"));
	}

	@Test(expected = RuntimeException.class) public void throwsExceptionOnInvalidInput()
	{
		abbr("d:\n  - a\n  b: c\n");
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
