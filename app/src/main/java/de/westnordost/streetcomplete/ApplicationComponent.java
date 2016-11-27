package de.westnordost.streetcomplete;

import javax.inject.Singleton;

import dagger.Component;
import de.westnordost.streetcomplete.data.DbModule;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.data.QuestChangesUploadService;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.meta.MetadataModule;
import de.westnordost.streetcomplete.quests.opening_hours.OpeningHoursPerWeek;
import de.westnordost.streetcomplete.quests.road_name.AutoCorrectAbbreviationsEditText;
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm;
import de.westnordost.streetcomplete.util.SerializedSavedState;

@Singleton
@Component(modules = {ApplicationModule.class, OsmModule.class, DbModule.class, MetadataModule.class})
public interface ApplicationComponent
{
	void inject(MainActivity mainActivity);
	void inject(AutoCorrectAbbreviationsEditText autoCorrectAbbreviationsEditText);
	void inject(NoteDiscussionForm noteDiscussionForm);
	void inject(OpeningHoursPerWeek openingHoursPerWeek);
	void inject(SerializedSavedState tSerializedSavedState);

	void inject(QuestChangesUploadService questChangesUploadService);
	void inject(QuestDownloadService questChangesDownloadService);
}
