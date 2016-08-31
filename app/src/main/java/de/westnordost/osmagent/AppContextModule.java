package de.westnordost.osmagent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;

@Module
public class AppContextModule
{
	private Context context;

	public AppContextModule(Context context)
	{
		this.context = context;
	}

	@Provides public Context provideContext()
	{
		return context;
	}

	@Provides public SharedPreferences providePrefs()
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}
