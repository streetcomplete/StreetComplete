package de.westnordost.osmagent;

public enum Injector
{
	instance;

	private ApplicationComponent applicationComponent;

	void initializeApplicationComponent(OsmagentApplication app)
	{
		applicationComponent = DaggerApplicationComponent.builder()
				.applicationModule(new ApplicationModule(app))
				.build();
	}

	public ApplicationComponent getApplicationComponent() {
		return applicationComponent;
	}
}
