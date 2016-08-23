package de.westnordost.osmagent.meta;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.R;

public class LocaleMetadata
{
	private static LocaleMetadata singleton;

	/** get the singleton instance of LocaleMetadata (valid for whole application) */
	public static LocaleMetadata getInstance(Context context)
	{
		Context applicationContext = context.getApplicationContext();
		if(singleton == null || singleton.ctx.get() == null)
		{
			singleton = new LocaleMetadata(applicationContext);
		}
		return singleton;
	}

	private WeakReference<Context> ctx;

	private LanguagesByCountry languagesByCountry;
	private Abbreviations abbreviations;

	public LocaleMetadata(Context ctx)
	{
		this.ctx = new WeakReference<>(ctx);
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

		InputStream is = ctx.get().getResources().openRawResource(R.raw.country_codes);
		return new LanguagesByCountry(is);
	}

	/** @return abbreviations for the country the user is in currently */
	public Abbreviations getCurrentCountryAbbreviations()
	{
		Locale currentLocale = getCurrentCountryLocale();
		if(abbreviations == null || !abbreviations.getLocale().equals(currentLocale))
		{
			abbreviations = createCurrentCountryAbbreviations();
		}
		return abbreviations;
	}

	private synchronized Abbreviations createCurrentCountryAbbreviations()
	{
		Locale locale = getCurrentCountryLocale();

		// double check in synchronized block
		if(abbreviations != null && abbreviations.getLocale().equals(locale)) return abbreviations;

		Configuration configuration = getLocaleConfiguration(locale);

		InputStream is = ctx.get().createConfigurationContext(configuration).getResources().
				openRawResource(R.raw.abbreviations);

		return new Abbreviations(is, locale);
	}

	private Configuration getLocaleConfiguration(Locale locale)
	{
		Configuration configuration = new Configuration(ctx.get().getResources().getConfiguration());
		configuration.setLocale(locale);
		return configuration;
	}

	/** Find the locale of the country the user is in currently. */
	public Locale getCurrentCountryLocale()
	{
		// always fall back to default locale if the current country cannot be found
		Locale locale = Locale.getDefault();
		String countryCode = findCurrentCountry();

		if(countryCode != null)
		{
			List<String> languages = getLanguagesByCountry().get(countryCode);
			// for countries with several official languages, the chance is higher that the user is
			// in an area in which the language he set in his preferences is spoken than not
			if(languages.size() > 1 && languages.indexOf(locale.getLanguage()) != -1)
			{
				locale = new Locale(locale.getLanguage(), countryCode);
			}
			// otherwise, use the most common one
			else if(languages.size() > 0)
			{
				locale = new Locale(languages.get(0), countryCode);
			}
		}
		return locale;
	}

	/** Find the country the user is in currently. Returns null if the country cannot be determined */
	private String findCurrentCountry()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.get());
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
		if(networkCountry == null) return null;

		return networkCountry.toUpperCase(Locale.US);
	}

	private String getTelephonySimCountry()
	{
		TelephonyManager tm = getTelephonyManager();
		if(tm == null) return null;

		String userCountry = tm.getSimCountryIso();
		if (userCountry == null) return null;

		return userCountry.toUpperCase(Locale.US);
	}

	private TelephonyManager getTelephonyManager()
	{
		return (TelephonyManager) ctx.get().getSystemService(Context.TELEPHONY_SERVICE);
	}
}
