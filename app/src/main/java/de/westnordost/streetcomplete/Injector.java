package de.westnordost.streetcomplete;

import de.westnordost.streetcomplete.data.user.achievements.AchievementsModule;

public enum Injector
{
	instance;

	private ApplicationComponent applicationComponent;

	void initializeApplicationComponent(StreetCompleteApplication app)
	{
		applicationComponent = DaggerApplicationComponent.builder()
				.applicationModule(new ApplicationModule(app))
				.achievementsModule(AchievementsModule.INSTANCE)
				.build();
	}

	public ApplicationComponent getApplicationComponent() {
		return applicationComponent;
	}
}
