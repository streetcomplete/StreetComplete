package de.westnordost.streetcomplete;

import android.app.Application;

import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.meta.CountryBoundaries;

public class StreetCompleteApplication extends Application
{
	@Inject FutureTask<CountryBoundaries> countryBoundariesFuture;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Injector.instance.initializeApplicationComponent(this);
		Injector.instance.getApplicationComponent().inject(this);
		preload();
	}

	/** Load some things in the background that are needed later */
	private void preload()
	{
		// country boundaries are necessary latest for when a quest is opened
		new Thread(countryBoundariesFuture).start();
	}
}
