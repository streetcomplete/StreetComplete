package de.westnordost.streetcomplete;

import android.app.Application;

public class StreetCompleteApplication extends Application
{
	private PerApplicationStartPrefs perApplicationStartPrefs = new PerApplicationStartPrefs();

	@Override
	public void onCreate()
	{
		super.onCreate();
		Injector.instance.initializeApplicationComponent(this);
	}

	/** A bundle that is maintained as long as the application is active (and reset after it is
	 *  started anew). Used for values that should be persisted beyond the lifetime of single
	 *  activities but not persisted in shared prefs*/
	public PerApplicationStartPrefs getPerApplicationStartPrefs()
	{
		return perApplicationStartPrefs;
	}
}
