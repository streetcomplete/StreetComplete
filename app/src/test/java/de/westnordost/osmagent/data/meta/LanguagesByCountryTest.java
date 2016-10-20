package de.westnordost.osmagent.data.meta;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class LanguagesByCountryTest extends TestCase
{
	public void testOneLanguage()
	{
		List<String> languages = langs("DE: de").get("DE");
		assertEquals(1, languages.size());
		assertEquals("de", languages.get(0));
	}

	public void testTwoLanguages()
	{
		List<String> languages = langs("DE: [de,es]").get("DE");
		assertEquals(2, languages.size());
		assertEquals("de", languages.get(0));
		assertEquals("es", languages.get(1));
	}

	public void testNoLanguage()
	{
		assertEquals(0, langs("AQ: ").get("AQ").size());
	}

	private LanguagesByCountry langs(String input)
	{
		try
		{
			return new LanguagesByCountry(new ByteArrayInputStream(input.getBytes("utf-8")));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
