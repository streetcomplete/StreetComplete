package de.westnordost.streetcomplete;

public class Injector
{
	private static Injector instance;
	public static Injector getInstance()
	{
		if(instance == null)
		{
			instance = new Injector();
		}
		return instance;
	}

	/** For tests only */
	static void setInstance(Injector instance)
	{
		Injector.instance = instance;
	}

	private ApplicationComponent applicationComponent;

	void initializeApplicationComponent(StreetCompleteApplication app)
	{
		applicationComponent = DaggerApplicationComponent.builder()
				.applicationModule(new ApplicationModule(app))
				.build();
	}

	public ApplicationComponent getApplicationComponent() {
		return applicationComponent;
	}
}
