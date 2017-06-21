package de.westnordost.streetcomplete.data.meta;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;

public class AbbreviationsByLocale
{
	private final Context applicationContext;

	private Map<String,Abbreviations> byLanguageAbbreviations = new HashMap<>();

	@Inject public AbbreviationsByLocale(Context applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	public Abbreviations get(Locale locale)
	{
		String code = locale.toString();
		if(!byLanguageAbbreviations.containsKey(code))
		{
			byLanguageAbbreviations.put(code, load(locale));
		}
		return byLanguageAbbreviations.get(code);
	}

	private Abbreviations load(Locale locale)
	{
		InputStream is = getResources(locale).openRawResource(R.raw.abbreviations);
		return new Abbreviations(is, locale);
	}

	private Resources getResources(Locale locale)
	{
		Configuration configuration = new Configuration(applicationContext.getResources().getConfiguration());
		configuration.setLocale(locale);
		return applicationContext.createConfigurationContext(configuration).getResources();
	}
}
