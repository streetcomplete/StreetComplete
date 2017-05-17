package de.westnordost.streetcomplete.data.meta;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;

public class CurrentCountry
{
	private final Context applicationContext;

	private LanguagesByCountry languagesByCountry;

	@Inject public CurrentCountry(Context applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	private LanguagesByCountry getLanguagesByCountry()
	{
		if(languagesByCountry == null)
		{
			languagesByCountry = createLanguagesByCountry();
		}
		return languagesByCountry;
	}

	private synchronized LanguagesByCountry createLanguagesByCountry()
	{
		// double check in synchronized block
		if(languagesByCountry != null) return languagesByCountry;

		InputStream is = applicationContext.getResources().openRawResource(R.raw.country_codes);
		return new LanguagesByCountry(is);
	}

	public Resources getResources()
	{
		Locale locale = getLocale();

		Configuration configuration = getLocaleConfiguration(locale);

		return applicationContext.createConfigurationContext(configuration).getResources();
	}

	private Configuration getLocaleConfiguration(Locale locale)
	{
		Configuration configuration = new Configuration(applicationContext.getResources().getConfiguration());
		configuration.setLocale(locale);
		return configuration;
	}

	/** Find the locale of the country the user is in currently. */
	public Locale getLocale()
	{
		// always fall back to default locale if the current country cannot be found
		Locale locale = Locale.getDefault();
		String countryCode = getCountry();

		if(countryCode != null)
		{
			List<String> languages = getLanguagesByCountry().get(countryCode);
			// #53: Android could also return an invalid or non-existing countryCode, we can't trust it here
			if(languages != null)
			{
				// for countries with several official languages, the chance is higher that the user is
				// in an area in which the language he set in his preferences is spoken than not
				if (languages.size() > 1 && languages.indexOf(locale.getLanguage()) != -1)
				{
					locale = new Locale(locale.getLanguage(), countryCode);
				}
				// otherwise, use the most common one
				else if (languages.size() > 0)
				{
					locale = new Locale(languages.get(0), countryCode);
				}
			}
		}
		return locale;
	}

	/** Find the country the user is in currently. Returns null if the country cannot be determined */
	public String getCountry()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		String lastCountry = prefs.getString(Prefs.CURRENT_COUNTRY, null);
		String networkCountry = getTelephonyNetworkCountry();

		// note: the telephony stuff will only work for mobile phones, not for tablets without SIM
		// card. Perhaps additionally use geocoding by last position here ...
		String currentCountry;
		if(networkCountry != null)
		{
			currentCountry = networkCountry;
		}
		else if(lastCountry != null)
		{
			currentCountry = lastCountry;
		}
		else
		{
			// fallback: if the user is not connected to a mobile network, the chance is high that
			// he is in the country where he bought the SIM card
			currentCountry = getTelephonySimCountry();
		}

		if(currentCountry != null && !currentCountry.equals(lastCountry))
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(Prefs.CURRENT_COUNTRY, currentCountry);
			editor.apply();
		}

		return currentCountry;
	}

	private String getTelephonyNetworkCountry()
	{
		TelephonyManager tm = getTelephonyManager();
		if(tm == null) return null;

		// is documented to be unreliable
		if(tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) return null;

		String networkCountry = tm.getNetworkCountryIso();
		if(networkCountry == null || networkCountry.isEmpty()) return null;

		return networkCountry.toUpperCase(Locale.US);
	}

	private String getTelephonySimCountry()
	{
		TelephonyManager tm = getTelephonyManager();
		if(tm == null) return null;

		String userCountry = tm.getSimCountryIso();
		if (userCountry == null || userCountry.isEmpty()) return null;

		return userCountry.toUpperCase(Locale.US);
	}

	private TelephonyManager getTelephonyManager()
	{
		return (TelephonyManager) applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
}
