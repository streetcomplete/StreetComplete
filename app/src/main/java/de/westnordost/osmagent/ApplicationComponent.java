package de.westnordost.osmagent;

import javax.inject.Singleton;

import dagger.Component;
import de.westnordost.osmagent.data.DbModule;
import de.westnordost.osmagent.data.OsmModule;
import de.westnordost.osmagent.data.QuestChangesUploader;
import de.westnordost.osmagent.data.QuestDownloader;
import de.westnordost.osmagent.dialogs.AutoCorrectAbbreviationsEditText;
import de.westnordost.osmagent.dialogs.NoteDiscussionForm;

@Singleton
@Component(modules = {ApplicationModule.class, OsmModule.class, DbModule.class})
public interface ApplicationComponent
{
	void inject(MainActivity mainActivity);
	void inject(AutoCorrectAbbreviationsEditText autoCorrectAbbreviationsEditText);
	void inject(NoteDiscussionForm noteDiscussionForm);

	QuestChangesUploader questChangesUploader();
	QuestDownloader questDownloader();
}
