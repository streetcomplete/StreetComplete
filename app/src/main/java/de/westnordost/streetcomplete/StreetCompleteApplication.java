package de.westnordost.streetcomplete;

import android.app.Application;

public class StreetCompleteApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		Injector.instance.initializeApplicationComponent(this);
	}
}
