package de.westnordost.streetcomplete.data.meta;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Road abbreviations for all languages */
public class Abbreviations
{
	private Map<String, String> abbreviations;
	private Locale locale;

	public Abbreviations(InputStream config, Locale locale)
	{
		this.locale = locale;
		try
		{
			parseConfig(config);
		}
		catch (YamlException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void parseConfig(InputStream config) throws YamlException
	{
		abbreviations = new HashMap<>();

		YamlReader reader = new YamlReader(new InputStreamReader(config));
		Map map = (Map) reader.read();
		for(Object o : map.entrySet())
		{
			Map.Entry pair2 = (Map.Entry) o;
			String abbreviation = ((String)pair2.getKey()).toLowerCase(locale);
			String expansion = ((String) pair2.getValue()).toLowerCase(locale);

			if(abbreviation.endsWith("$"))
			{
				abbreviation = abbreviation.substring(0, abbreviation.length() - 1) + "\\.?$";
			}
			else
			{
				abbreviation += "\\.?";
			}

			if(abbreviation.startsWith("..."))
			{
				abbreviation = "(\\w*)" + abbreviation.substring(3);
				expansion = "$1" + expansion;
			}
			abbreviations.put(abbreviation, expansion);
		}
	}

	/**
	 *  @param word the word that might be an abbreviation for something
	 *  @param isFirstWord whether the given word is the first word in the name
	 *  @param isLastWord whether the given word is the last word in the name
	 *  @return the expansion of the abbreviation if word is an abbreviation for something,
	 *          otherwise null*/
	public String getExpansion(String word, boolean isFirstWord, boolean isLastWord)
	{
		for(Map.Entry<String, String> abbreviation : abbreviations.entrySet())
		{
			String pattern = abbreviation.getKey();

			Matcher m = getMatcher(word, pattern, isFirstWord, isLastWord);
			if(m == null || !m.matches()) continue;

			String replacement = abbreviation.getValue();
			String result = m.replaceFirst(replacement);

			return firstLetterToUppercase(result,locale);
		}
		return null;
	}

	private Matcher getMatcher(String word, String pattern, boolean isFirstWord, boolean isLastWord)
	{
		if(pattern.startsWith("^") && !isFirstWord) return null;
		if(pattern.endsWith("$") && !isLastWord) return null;

		int patternFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		Pattern p = Pattern.compile(pattern, patternFlags);
		Matcher m = p.matcher(word);

		if(pattern.endsWith("$") )
		{
			/* abbreviations that are marked to only appear at the end of the name do not
			   match with the first word the user is typing. I.e. if the user types "St. ", it will
			   not expand to "Street " because it is also the first and only word so far

			   UNLESS the word is actually concatenated, i.e. German "Königstr." is expanded to
			   "Königstraße" (but "Str. " is not expanded to "Straße") */
			if(isFirstWord)
			{
				boolean isConcatenated = m.matches() && m.groupCount() > 0 && !m.group(1).isEmpty();
				if(!isConcatenated)
					return null;
			}
		}

		return m;
	}

	public Locale getLocale()
	{
		return locale;
	}

	private static String firstLetterToUppercase(String word, Locale locale)
	{
		return word.substring(0,1).toUpperCase(locale) + word.substring(1);
	}

	/** @return whether any word in the given name matches with an abbreviation */
	public boolean containsAbbreviations(String name)
	{
		String[] words = name.split("[ -]+");
		for (int i=0; i<words.length; ++i)
		{
			String word = words[i];
			for (Map.Entry<String, String> abbreviation : abbreviations.entrySet())
			{
				String pattern = abbreviation.getKey();
				Matcher m = getMatcher(word, pattern, i==0, i==words.length - 1);
				if(m != null && m.matches())
					return true;
			}
		}
		return false;
	}
}
