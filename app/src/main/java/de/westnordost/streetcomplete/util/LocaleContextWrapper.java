package de.westnordost.streetcomplete.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.preference.PreferenceManager;

import java.util.Locale;

import de.westnordost.streetcomplete.Prefs;

/**
 * @source https://stackoverflow.com/a/40704077
 */
public class LocaleContextWrapper extends ContextWrapper {

	public LocaleContextWrapper(Context base) {
		super(base);
	}

	@SuppressWarnings("deprecation")
	public static ContextWrapper wrap(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String language = prefs.getString(Prefs.LANGUAGE_SELECT, "");

		Configuration config = context.getResources().getConfiguration();
		Locale sysLocale = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
			sysLocale = getSystemLocale(config);
		} else {
			sysLocale = getSystemLocaleLegacy(config);
		}
		if (!language.equals("") && !sysLocale.getLanguage().equals(language)) {
			Locale locale = new Locale(language);
			Locale.setDefault(locale);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				setSystemLocale(config, locale);
			} else {
				setSystemLocaleLegacy(config, locale);
			}

		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			context = context.createConfigurationContext(config);
		} else {
			context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
		}
		return new LocaleContextWrapper(context);
	}

	@SuppressWarnings("deprecation")
	public static Locale getSystemLocaleLegacy(Configuration config){
		return config.locale;
	}

	@TargetApi(Build.VERSION_CODES.N)
	public static Locale getSystemLocale(Configuration config){
		return config.getLocales().get(0);
	}

	@SuppressWarnings("deprecation")
	public static void setSystemLocaleLegacy(Configuration config, Locale locale){
		config.locale = locale;
	}

	@TargetApi(Build.VERSION_CODES.N)
	public static void setSystemLocale(Configuration config, Locale locale){
		config.setLocale(locale);
	}
}
