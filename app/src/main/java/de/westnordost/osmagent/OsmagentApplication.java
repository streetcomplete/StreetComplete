package de.westnordost.osmagent;

import android.app.Application;

public class OsmagentApplication extends Application
{
	private ApplicationComponent applicationComponent;

	@Override
	public void onCreate()
	{
		super.onCreate();
		applicationComponent = DaggerApplicationComponent.builder()
					.applicationModule(new ApplicationModule(this))
					.build();
	}

	public ApplicationComponent component()
	{
		return applicationComponent;
	}
}
