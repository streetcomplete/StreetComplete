package de.westnordost.osmagent;

import android.app.Application;

public class OsmagentApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		Injector.instance.initializeApplicationComponent(this);
	}
}
