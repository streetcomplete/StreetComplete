package de.westnordost.streetcomplete;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Component;
import de.westnordost.streetcomplete.data.DbModule;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.data.osm.upload.ChangesetAutoCloserWorker;
import de.westnordost.streetcomplete.data.upload.QuestChangesUploadService;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.meta.MetadataModule;
import de.westnordost.streetcomplete.map.MainFragment;
import de.westnordost.streetcomplete.oauth.OAuthModule;
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.QuestModule;
import de.westnordost.streetcomplete.quests.SplitWayFragment;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevelsForm;
import de.westnordost.streetcomplete.quests.localized_name.AAddLocalizedNameForm;
import de.westnordost.streetcomplete.quests.oneway.AddOnewayForm;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm;
import de.westnordost.streetcomplete.quests.localized_name.AddRoadNameForm;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm;
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm;
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddCollectionTimesForm;
import de.westnordost.streetcomplete.settings.SettingsActivity;
import de.westnordost.streetcomplete.settings.SettingsFragment;
import de.westnordost.streetcomplete.settings.ShowQuestFormsActivity;
import de.westnordost.streetcomplete.settings.questselection.QuestSelectionFragment;
import de.westnordost.streetcomplete.statistics.AnswersCounter;
import de.westnordost.streetcomplete.map.QuestsMapFragment;
import de.westnordost.streetcomplete.util.SerializedSavedState;

@Singleton
@Component(modules = {ApplicationModule.class, OAuthModule.class, OsmModule.class, QuestModule.class,
		DbModule.class, MetadataModule.class})
public interface ApplicationComponent
{
	void inject(StreetCompleteApplication app);

	void inject(MainActivity mainActivity);
	void inject(NoteDiscussionForm noteDiscussionForm);
	void inject(SerializedSavedState tSerializedSavedState);

	void inject(QuestChangesUploadService questChangesUploadService);
	void inject(QuestDownloadService questChangesDownloadService);

	void inject(SettingsFragment settingsFragment);
	void inject(SettingsActivity settingsActivity);

	void inject(AnswersCounter answersCounter);

	void inject(AddOpeningHoursForm addOpeningHoursForm);
	void inject(AddRoadNameForm addRoadNameForm);
	void inject(AddParkingFeeForm parkingFeeForm);
	void inject(AddOnewayForm addOnewayForm);
	void inject(AddCollectionTimesForm addCollectionTimesForm);

	void inject(OsmOAuthDialogFragment osmOAuthDialogFragment);

	void inject(AAddLocalizedNameForm.InjectedFields fields);
	void inject(AbstractQuestAnswerFragment.InjectedFields fields);

	void inject(QuestsMapFragment questsMapFragment);

	void inject(QuestSelectionFragment questSelectionFragment);

	void inject(AddBuildingLevelsForm fragment);
	void inject(ChangesetAutoCloserWorker worker);

	void inject(@NonNull SplitWayFragment splitWayFragment);

	void inject(@NotNull ShowQuestFormsActivity showQuestFormsActivity);

	void inject(@NotNull MainFragment mainFragment);
}
