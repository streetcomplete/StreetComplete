package de.westnordost.streetcomplete

import dagger.Component
import de.westnordost.streetcomplete.controls.*
import de.westnordost.streetcomplete.data.DbModule
import de.westnordost.streetcomplete.data.OsmApiModule
import de.westnordost.streetcomplete.data.upload.UploadModule2
import de.westnordost.streetcomplete.data.download.DownloadModule
import de.westnordost.streetcomplete.data.download.QuestDownloadService
import de.westnordost.streetcomplete.data.meta.MetadataModule
import de.westnordost.streetcomplete.data.osm.upload.changesets.ChangesetAutoCloserWorker
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesModule
import de.westnordost.streetcomplete.data.upload.UploadModule
import de.westnordost.streetcomplete.data.upload.UploadService
import de.westnordost.streetcomplete.data.user.UserModule
import de.westnordost.streetcomplete.data.user.achievements.AchievementsModule
import de.westnordost.streetcomplete.map.MainFragment
import de.westnordost.streetcomplete.map.QuestsMapFragment
import de.westnordost.streetcomplete.notifications.OsmUnreadMessagesFragment
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.QuestModule
import de.westnordost.streetcomplete.quests.SplitWayFragment
import de.westnordost.streetcomplete.quests.address.AddAddressStreetForm;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevelsForm
import de.westnordost.streetcomplete.quests.localized_name.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.localized_name.AddRoadNameForm
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm
import de.westnordost.streetcomplete.quests.oneway.AddOnewayForm
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsModule
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddCollectionTimesForm
import de.westnordost.streetcomplete.settings.OAuthFragment
import de.westnordost.streetcomplete.settings.SettingsActivity
import de.westnordost.streetcomplete.settings.SettingsFragment
import de.westnordost.streetcomplete.settings.ShowQuestFormsActivity
import de.westnordost.streetcomplete.settings.questselection.QuestSelectionFragment
import de.westnordost.streetcomplete.user.*
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class,
    UploadModule2::class,
    OsmApiModule::class,
    TrafficFlowSegmentsModule::class,
    OsmNotesModule::class,
    UploadModule::class,
    DownloadModule::class,
    QuestModule::class,
    DbModule::class,
    MetadataModule::class,
    UserModule::class,
    AchievementsModule::class
])
interface ApplicationComponent {
    fun inject(app: StreetCompleteApplication)
    fun inject(mainActivity: MainActivity)
    fun inject(noteDiscussionForm: NoteDiscussionForm)
    fun inject(uploadService: UploadService)
    fun inject(questChangesDownloadService: QuestDownloadService)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(addOpeningHoursForm: AddOpeningHoursForm)
    fun inject(addRoadNameForm: AddRoadNameForm)
    fun inject(addAddressStreetForm: AddAddressStreetForm)
    fun inject(parkingFeeForm: AddParkingFeeForm)
    fun inject(addOnewayForm: AddOnewayForm)
    fun inject(addCollectionTimesForm: AddCollectionTimesForm)
    fun inject(OAuthFragment: OAuthFragment)
    fun inject(questStatisticsFragment: QuestStatisticsFragment)
    fun inject(fields: AAddLocalizedNameForm.InjectedFields)
    fun inject(fields: AbstractQuestAnswerFragment.InjectedFields)
    fun inject(questsMapFragment: QuestsMapFragment)
    fun inject(questSelectionFragment: QuestSelectionFragment)
    fun inject(fragment: AddBuildingLevelsForm)
    fun inject(worker: ChangesetAutoCloserWorker)
    fun inject(splitWayFragment: SplitWayFragment)
    fun inject(showQuestFormsActivity: ShowQuestFormsActivity)
    fun inject(mainFragment: MainFragment)
    fun inject(achievementsFragment: AchievementsFragment)
    fun inject(linksFragment: LinksFragment)
    fun inject(profileFragment: ProfileFragment)
    fun inject(userActivity: UserActivity)
    fun inject(loginFragment: LoginFragment)
    fun inject(osmUnreadMessagesFragment: OsmUnreadMessagesFragment)
    fun inject(notificationButtonFragment: NotificationButtonFragment)
    fun inject(undoButtonFragment: UndoButtonFragment)
    fun inject(uploadButtonFragment: UploadButtonFragment)
    fun inject(answersCounterFragment: AnswersCounterFragment)
    fun inject(questDownloadProgressFragment: QuestDownloadProgressFragment)
    fun inject(questStatisticsByCountryFragment: QuestStatisticsByCountryFragment)
    fun inject(questStatisticsByQuestTypeFragment: QuestStatisticsByQuestTypeFragment)
}