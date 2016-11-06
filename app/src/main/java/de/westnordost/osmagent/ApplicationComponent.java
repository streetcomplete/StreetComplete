package de.westnordost.osmagent;

import javax.inject.Singleton;

import dagger.Component;
import de.westnordost.osmagent.data.DbModule;
import de.westnordost.osmagent.data.OsmModule;
import de.westnordost.osmagent.data.QuestChangesUploader;
import de.westnordost.osmagent.data.QuestDownloader;
import de.westnordost.osmagent.data.meta.MetadataModule;
import de.westnordost.osmagent.quests.opening_hours.OpeningHoursPerWeek;
import de.westnordost.osmagent.quests.road_name.AutoCorrectAbbreviationsEditText;
import de.westnordost.osmagent.quests.note_discussion.NoteDiscussionForm;
import de.westnordost.osmagent.util.SerializedSavedState;

@Singleton
@Component(modules = {ApplicationModule.class, OsmModule.class, DbModule.class, MetadataModule.class})
public interface ApplicationComponent
{
	void inject(MainActivity mainActivity);
	void inject(AutoCorrectAbbreviationsEditText autoCorrectAbbreviationsEditText);
	void inject(NoteDiscussionForm noteDiscussionForm);
	void inject(OpeningHoursPerWeek openingHoursPerWeek);
	void inject(SerializedSavedState tSerializedSavedState);

	QuestChangesUploader questChangesUploader();
	QuestDownloader questDownloader();
}
