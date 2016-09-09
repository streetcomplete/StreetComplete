package de.westnordost.osmagent;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent
{
	Context appContext();
	//QuestController makeQuestController();
	// TODO
}
