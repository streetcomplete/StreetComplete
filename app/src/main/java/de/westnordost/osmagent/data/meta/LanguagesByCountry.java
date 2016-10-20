package de.westnordost.osmagent.data.meta;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Official languages by country */
public class LanguagesByCountry
{
	private Map<String, List<String>> languagesByCountry;

	public LanguagesByCountry(InputStream config)
	{
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
		languagesByCountry = new HashMap<>();
		YamlReader reader = new YamlReader(new InputStreamReader(config));
		Map map = (Map) reader.read();
		for (Object o : map.entrySet())
		{
			Map.Entry pair = (Map.Entry) o;
			String country = ((String)pair.getKey()).toUpperCase(Locale.US);
			List<String> languages = asList(pair.getValue());
			languagesByCountry.put(country, languages);
		}
	}

	private static List<String> asList(Object input)
	{
		if(input instanceof String)
		{
			String inputString = (String) input;
			if(inputString.isEmpty()) return Collections.emptyList();
			return Arrays.asList(inputString);
		}
		if(input instanceof List)
		{
			List<String> inputList = (List<String>) input;
			return Arrays.asList(inputList.toArray(new String[inputList.size()]));
		}
		throw new RuntimeException("Expected either a string or a list");
	}

	/** Get list of official languages (ISO 639-1) by country (ISO 3166 alpha-2) */
	public List<String> get(String countryCode)
	{
		countryCode = countryCode.toUpperCase(Locale.US);
		return languagesByCountry.get(countryCode);
	}
}
