package de.westnordost.osmagent;

import javax.inject.Singleton;

import dagger.Component;
import de.westnordost.osmagent.quests.DbModule;
import de.westnordost.osmagent.quests.OsmModule;
import de.westnordost.osmagent.quests.QuestChangesUploader;
import de.westnordost.osmagent.quests.QuestDownloader;
import de.westnordost.osmagent.quests.dialogs.AutoCorrectAbbreviationsEditText;

@Singleton
@Component(modules = {ApplicationModule.class, OsmModule.class, DbModule.class})
public interface ApplicationComponent
{
	void inject(MainActivity mainActivity);
	void inject(AutoCorrectAbbreviationsEditText autoCorrectAbbreviationsEditText);

	QuestChangesUploader questChangesUploader();
	QuestDownloader questDownloader();
}
