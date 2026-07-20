package de.westnordost.streetcomplete

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants.USE_TEST_API
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.data.download.strategy.MobileDataAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.strategy.WifiAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsDao
import de.westnordost.streetcomplete.data.logs.LogsSource
import de.westnordost.streetcomplete.data.messages.MessagesSource
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsControllerImpl
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsDao
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsSource
import de.westnordost.streetcomplete.data.osm.edits.EditElementsDao
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsControllerImpl
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsDao
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderDao
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSourceImpl
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditUploader
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiClient
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiClientImpl
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiSerializer
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.PolylinesSerializer
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryDao
import de.westnordost.streetcomplete.data.osm.mapdata.ElementDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClientImpl
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiParser
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiSerializer
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataControllerImpl
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataDownloader
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataSource
import de.westnordost.streetcomplete.data.osm.mapdata.NodeDao
import de.westnordost.streetcomplete.data.osm.mapdata.RelationDao
import de.westnordost.streetcomplete.data.osm.mapdata.WayDao
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestDao
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsController
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsDao
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsSource
import de.westnordost.streetcomplete.data.osmcal.OsmCalApiClient
import de.westnordost.streetcomplete.data.osmcal.OsmCalApiClientImpl
import de.westnordost.streetcomplete.data.osmcal.OsmCalParser
import de.westnordost.streetcomplete.data.osmcal.OsmCalUpdater
import de.westnordost.streetcomplete.data.osmnotes.AvatarsDownloader
import de.westnordost.streetcomplete.data.osmnotes.AvatarsInNotesUpdater
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NoteControllerImpl
import de.westnordost.streetcomplete.data.osmnotes.NoteDao
import de.westnordost.streetcomplete.data.osmnotes.NoteSource
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClientImpl
import de.westnordost.streetcomplete.data.osmnotes.NotesApiParser
import de.westnordost.streetcomplete.data.osmnotes.NotesDownloader
import de.westnordost.streetcomplete.data.osmnotes.PhotoServiceApiClient
import de.westnordost.streetcomplete.data.osmnotes.PhotoServiceApiClientImpl
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsControllerImpl
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsDao
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsUploader
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSourceImpl
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.osmtracks.TracksApiClient
import de.westnordost.streetcomplete.data.osmtracks.TracksApiClientImpl
import de.westnordost.streetcomplete.data.osmtracks.TracksSerializer
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.presets.EditTypePresetsDao
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.data.upload.VersionIsBannedChecker
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.data.user.UserApiClient
import de.westnordost.streetcomplete.data.user.UserApiClientImpl
import de.westnordost.streetcomplete.data.user.UserApiParser
import de.westnordost.streetcomplete.data.user.UserDataController
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.AchievementsController
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsDao
import de.westnordost.streetcomplete.data.user.achievements.UserLinksDao
import de.westnordost.streetcomplete.data.user.achievements.achievements
import de.westnordost.streetcomplete.data.user.achievements.editTypeAliases
import de.westnordost.streetcomplete.data.user.achievements.links
import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClient
import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClientImpl
import de.westnordost.streetcomplete.data.user.statistics.ActiveDatesDao
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsDao
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTable
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsDao
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsTable
import de.westnordost.streetcomplete.data.user.statistics.StatisticsApiClient
import de.westnordost.streetcomplete.data.user.statistics.StatisticsApiClientImpl
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import de.westnordost.streetcomplete.data.user.statistics.StatisticsControllerImpl
import de.westnordost.streetcomplete.data.user.statistics.StatisticsParser
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderDao
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenControllerImpl
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterController
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterControllerImpl
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeDao
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import de.westnordost.streetcomplete.data.weeklyosm.WeeklyOsmApiClient
import de.westnordost.streetcomplete.data.weeklyosm.WeeklyOsmApiClientImpl
import de.westnordost.streetcomplete.data.weeklyosm.WeeklyOsmRssFeedParser
import de.westnordost.streetcomplete.data.weeklyosm.WeeklyOsmUpdater
import de.westnordost.streetcomplete.overlays.overlaysRegistry
import de.westnordost.streetcomplete.quests.questTypeRegistry
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.about.ChangelogViewModel
import de.westnordost.streetcomplete.screens.about.ChangelogViewModelImpl
import de.westnordost.streetcomplete.screens.about.CreditsViewModel
import de.westnordost.streetcomplete.screens.about.CreditsViewModelImpl
import de.westnordost.streetcomplete.screens.about.logs.LogsViewModel
import de.westnordost.streetcomplete.screens.about.logs.LogsViewModelImpl
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.MainViewModelImpl
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModelImpl
import de.westnordost.streetcomplete.screens.settings.SettingsViewModel
import de.westnordost.streetcomplete.screens.settings.SettingsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsViewModel
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.language_selection.LanguageSelectionViewModel
import de.westnordost.streetcomplete.screens.settings.language_selection.LanguageSelectionViewModelImpl
import de.westnordost.streetcomplete.screens.settings.messages.MessageSelectionViewModel
import de.westnordost.streetcomplete.screens.settings.messages.MessageSelectionViewModelImpl
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionViewModel
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionViewModelImpl
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsViewModel
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsViewModelImpl
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionViewModel
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionViewModelImpl
import de.westnordost.streetcomplete.screens.user.UserViewModel
import de.westnordost.streetcomplete.screens.user.UserViewModelImpl
import de.westnordost.streetcomplete.screens.user.achievements.AchievementsViewModel
import de.westnordost.streetcomplete.screens.user.achievements.AchievementsViewModelImpl
import de.westnordost.streetcomplete.screens.user.edits.EditStatisticsViewModel
import de.westnordost.streetcomplete.screens.user.edits.EditStatisticsViewModelImpl
import de.westnordost.streetcomplete.screens.user.links.LinksViewModel
import de.westnordost.streetcomplete.screens.user.links.LinksViewModelImpl
import de.westnordost.streetcomplete.screens.user.login.LoginViewModel
import de.westnordost.streetcomplete.screens.user.login.LoginViewModelImpl
import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModel
import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModelImpl
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureViewModel
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureViewModelImpl
import de.westnordost.streetcomplete.ui.util.measure.ArQuestsDisabler
import de.westnordost.streetcomplete.ui.util.photo.PhotosViewModel
import de.westnordost.streetcomplete.ui.util.photo.PhotosViewModelImpl
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundariesImpl
import de.westnordost.streetcomplete.util.error_reporting.ErrorReportBuilder
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.userAgent
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val OSM_API_URL_LIVE = "https://api.openstreetmap.org/api/0.6/"
private const val OSM_API_URL_TEST = "https://master.apis.dev.openstreetmap.org/api/0.6/"
val OSM_API_URL = if (USE_TEST_API) OSM_API_URL_TEST else OSM_API_URL_LIVE

private const val STATISTICS_BACKEND_URL = "https://streetcomplete.app/statistics/"

val commonModule = module {

    //region basic configuration

    single { HttpClient {
        defaultRequest {
            userAgent(ApplicationConstants.USER_AGENT)
        }
        install(ContentEncoding) {
            gzip()
            // deflate is broken in KTOR, see https://youtrack.jetbrains.com/issue/KTOR-6999/Deflate-ContentEncoder-incorrectly-uses-raw-DEFLATE
            // deflate()
            identity()
        }
    } }
    single<Res> { Res }
    single<FileSystem> { SystemFileSystem }

    factory { Cleaner(get(), get(), get(), get(), get(), get(), get()) }
    factory { CacheTrimmer(get(), get()) }
    factory { Preloader(get(named("CountryBoundariesLazy")), get(named("FeatureDictionaryLazy"))) }

    //endregion

    //region upload & download

    // upload

    single { UnsyncedChangesCountSource(get(), get()) }

    factory { VersionIsBannedChecker(get(), "https://streetcomplete.app/banned_versions.txt", ApplicationConstants.USER_AGENT) }

    single { Uploader(get(), get(), get(), get(), get(), get(), get(named("SerializeSync"))) }

    /* uploading and downloading should be serialized, i.e. may not run in parallel, to avoid
     * certain race-condition.
     *
     * Example:
     * A download of refreshed OSM data takes 10 seconds. While the download is executing, the user
     * solves a quest (based on the previously downloaded data) which is immediately uploaded,
     * resulting in the updated element to be persisted.
     * When the download finally finishes, it got the data from 10 seconds ago, before the element
     * has been updated. Thus, the old element overwrites the new one. */
    single(named("SerializeSync")) { Mutex() }

    single<UploadProgressSource> { get<Uploader>() }


    // download

    factory { DownloadedTilesDao(get()) }
    factory { MobileDataAutoDownloadStrategy(get(), get()) }
    factory { WifiAutoDownloadStrategy(get(), get()) }

    single { Downloader(get(), get(), get(), get(), get(), get(named("SerializeSync"))) }

    single<DownloadProgressSource> { get<Downloader>() }

    single<DownloadedTilesSource> { get<DownloadedTilesController>() }
    single { DownloadedTilesController(get()) }

    factory { FeedsUpdater(get(), get(), get(), get(), get()) }


    // OSM API client

    factory<MapDataApiClient> { MapDataApiClientImpl(get(), OSM_API_URL, get(), get(), get()) }
    factory<NotesApiClient> { NotesApiClientImpl(get(), OSM_API_URL, get(), get()) }
    factory<TracksApiClient> { TracksApiClientImpl(get(), OSM_API_URL, get(), get()) }
    factory<UserApiClient> { UserApiClientImpl(get(), OSM_API_URL, get(), get()) }
    factory<ChangesetApiClient> { ChangesetApiClientImpl(get(), OSM_API_URL, get(), get()) }

    factory { UserApiParser() }
    factory { NotesApiParser() }
    factory { TracksSerializer() }
    factory { MapDataApiParser() }
    factory { MapDataApiSerializer() }
    factory { ChangesetApiSerializer() }

    //endregion

    //region map data

    factory { ElementDao(get(), get(), get()) }
    factory { MapDataDownloader(get(), get()) }
    factory { NodeDao(get()) }
    factory { RelationDao(get()) }
    factory { WayDao(get()) }

    single<MapDataSource> { get<MapDataController>() }
    single<MapDataController> { MapDataControllerImpl(get(), get(), get(), get(), get(), get(), get()) }

    factory { ElementGeometryCreator() }
    factory { ElementGeometryDao(get(), get(), get()) }
    factory { WayGeometryDao(get(), get()) }
    factory { RelationGeometryDao(get(), get()) }
    factory { PolylinesSerializer() }

    //endregion

    //region notes

    factory { AvatarsDownloader(get(), get(), get(), get(named("AvatarsCacheDirectory"))) }
    factory { AvatarsInNotesUpdater(get()) }
    factory { NoteDao(get()) }
    factory { NotesDownloader(get(), get()) }
    factory<PhotoServiceApiClient> { PhotoServiceApiClientImpl(get(), get(), ApplicationConstants.SC_PHOTO_SERVICE_URL) }

    single<NoteSource> { get<NoteController>() }
    single<NoteController> {
        NoteControllerImpl(get()).apply {
            // on notes have been updated, avatar images should be downloaded (cached) referenced in note discussions
            addListener(get<AvatarsInNotesUpdater>())
        }
    }

    //endregion

    //region edits

    single { AllEditTypes(listOf(get<QuestTypeRegistry>(), get<OverlayRegistry>())) }

    single<EditHistorySource> { get<EditHistoryController>() }
    single { EditHistoryController(get(), get(), get(), get(), get(), get()) }

    //endregion

    //region map data edits

    factory { ElementEditUploader(get(), get(), get()) }

    factory { ElementEditsDao(get(), get()) }
    factory { ElementIdProviderDao(get()) }
    factory { OpenChangesetsDao(get()) }
    factory { EditElementsDao(get()) }

    single { OpenChangesetsManager(get(), get(), get(), get()) }

    single { ElementEditsUploader(get(), get(), get(), get(), get(), get()) }

    single<ElementEditsSource> { get<ElementEditsController>() }
    single<ElementEditsController> { ElementEditsControllerImpl(get(), get(), get(), get()) }
    single<MapDataWithEditsSource> { MapDataWithEditsSourceImpl(get(), get(), get()) }

    factory { CreatedElementsDao(get()) }

    single<CreatedElementsSource> { get<CreatedElementsController>() }
    single<CreatedElementsController> { CreatedElementsControllerImpl(get()) }

    //endregion

    //region note edits

    factory { NoteEditsDao(get()) }

    single { NoteEditsUploader(get(), get(), get(), get(), get(), get(), get()) }
    single<NoteEditsController> { NoteEditsControllerImpl(get()) }
    single<NoteEditsSource> { get<NoteEditsController>() }
    single<NotesWithEditsSource> { NotesWithEditsSourceImpl(get(), get(), get()) }

    //endregion

    //region quests

    single<QuestTypeRegistry> {
        val countryInfos = get<CountryInfos>()
        val countryBoundariesLazy = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy"))
        val featureDictionaryLazy = get<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy"))
        questTypeRegistry(
            get(),
            { countryInfos.get(countryBoundariesLazy.value, it) },
            { countryBoundariesLazy.value.getIds(it).firstOrNull() },
            { featureDictionaryLazy.value.getFeature(it) }
        )
    }

    factory { OsmQuestDao(get()) }
    factory { OsmQuestsHiddenDao(get()) }

    single<OsmQuestSource> { get<OsmQuestController>() }
    single { OsmQuestController(get(), get(), get(), get(), get(named("CountryBoundariesLazy"))) }

    factory { NoteQuestsHiddenDao(get()) }

    single<OsmNoteQuestSource> { get<OsmNoteQuestController>() }

    single { OsmNoteQuestController(get(), get(), get(), get()) }

    factory { ArQuestsDisabler(get(), get()) }

    //endregion

    //region quest visibility

    single { VisibleQuestsSource(get(), get(), get(), get(), get(), get(), get()) }

    factory { QuestTypeOrderDao(get()) }
    factory { VisibleEditTypeDao(get()) }

    single<QuestTypeOrderSource> { get<QuestTypeOrderController>() }
    single { QuestTypeOrderController(get(), get(), get()) }

    single<TeamModeQuestFilterSource> { get<TeamModeQuestFilterController>() }
    single<TeamModeQuestFilterController> { TeamModeQuestFilterControllerImpl(get(), get()) }

    single<QuestsHiddenSource> { get<QuestsHiddenController>() }
    single<QuestsHiddenController> { QuestsHiddenControllerImpl(get(), get()) }

    single<VisibleEditTypeSource> { get<VisibleEditTypeController>() }
    single { VisibleEditTypeController(get(), get(), get()) }

    //endregion

    //region metadata

    single { NameSuggestionsSource(get()) }
    single { CountryInfos(get()) }

    single<CountryBoundaries> { CountryBoundariesImpl(get()) }
    single<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")) {
        lazy { get() }
    }

    single<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")) {
        lazy { get<FeatureDictionary>() }
    }

    single { SurveyChecker() }

    //endregion

    //region overlays

    single<OverlayRegistry> {
        overlaysRegistry(
            { location ->
                val countryInfos = get<CountryInfos>()
                val countryBoundaries = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")).value
                countryInfos.get(countryBoundaries, location)
            },
            { location ->
                val countryBoundaries = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")).value
                countryBoundaries.getIds(location).firstOrNull()
            },
            { element ->
                get<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")).value.getFeature(element)
            }
        )
    }

    single<SelectedOverlaySource> { get<SelectedOverlayController>() }
    single { SelectedOverlayController(get(), get()) }

    //endregion

    //region logs & errors

    factory { LogsDao(get()) }

    single { LogsController(get()) }
    single<LogsSource> { get<LogsController>() }

    single { DatabaseLogger(get()) }

    factory { ErrorReportBuilder(get()) }

    //endregion

    //region messages

    single { MessagesSource(get(), get(), get(), get(), get(), get()) }

    //endregion

    //region settings

    single { UrlConfigController(get(), get(), get(), get(), get(), get()) }

    single { Preferences(get()) }

    single { ResurveyIntervalsUpdater(get()) }

    factory { EditTypePresetsDao(get()) }

    single<EditTypePresetsSource> { get<EditTypePresetsController>() }
    single { EditTypePresetsController(get(), get()) }

    //endregion

    //region user info & login

    single<UserDataSource> { get<UserDataController>() }
    single { UserDataController(get(), get()) }

    single<UserLoginSource> { get<UserLoginController>() }
    single<UserAccessTokenSource> { get<UserLoginController>() }
    single { UserLoginController(get()) }

    single { UserUpdater(get(), get(), get(), get(), get(), get()) }

    single<OAuthApiClient> { OAuthApiClientImpl(get()) }

    //endregion

    //region user statistics

    factory(named("EditTypeStatistics")) { EditTypeStatisticsDao(get(), EditTypeStatisticsTable.NAME) }
    factory(named("CountryStatistics")) { CountryStatisticsDao(get(), CountryStatisticsTable.NAME) }

    factory(named("EditTypeStatisticsCurrentWeek")) { EditTypeStatisticsDao(get(), EditTypeStatisticsTable.NAME_CURRENT_WEEK) }
    factory(named("CountryStatisticsCurrentWeek")) { CountryStatisticsDao(get(), CountryStatisticsTable.NAME_CURRENT_WEEK) }

    factory { ActiveDatesDao(get()) }

    factory { StatisticsParser(editTypeAliases) }
    factory<StatisticsApiClient> { StatisticsApiClientImpl(get(), STATISTICS_BACKEND_URL, get()) }

    single<StatisticsSource> { get<StatisticsController>() }
    single<StatisticsController> {
        StatisticsControllerImpl(
            editTypeStatisticsDao = get(named("EditTypeStatistics")),
            countryStatisticsDao = get(named("CountryStatistics")),
            currentWeekEditTypeStatisticsDao = get(named("EditTypeStatisticsCurrentWeek")),
            currentWeekCountryStatisticsDao = get(named("CountryStatisticsCurrentWeek")),
            activeDatesDao = get(),
            countryBoundaries = get(named("CountryBoundariesLazy")),
            prefs = get(),
            userLoginSource = get(),
        )
    }

    //endregion

    //region achievements & links

    factory { UserAchievementsDao(get()) }
    factory { UserLinksDao(get()) }

    single<AchievementsSource> { get<AchievementsController>() }
    single { AchievementsController(get(), get(), get(), get(), achievements, links) }

    //endregion

    //region weeklyOSM updates

    factory<WeeklyOsmApiClient> { WeeklyOsmApiClientImpl(get(), get()) }
    factory { WeeklyOsmUpdater(get(), get()) }
    factory { WeeklyOsmRssFeedParser() }

    //endregion

    //region calendar events

    factory<OsmCalApiClient> { OsmCalApiClientImpl(get(), get()) }
    factory { OsmCalParser() }
    factory { OsmCalUpdater(get(), get(), get()) }
    factory { CalendarEventsDao(get()) }
    single { CalendarEventsController(get()) }
    single<CalendarEventsSource> { get<CalendarEventsController>() }

    //endregion

    //region main screen view models

    viewModel<MainViewModel> {
        MainViewModelImpl(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }

    viewModel<EditHistoryViewModel> {
        EditHistoryViewModelImpl(get(), get(), get(named("FeatureDictionaryLazy")))
    }

    viewModel<PhotosViewModel> { PhotosViewModelImpl(get(), get()) }

    viewModel<ArMeasureViewModel> { ArMeasureViewModelImpl(get(), get(), get()) }

    //endregion

    //region user screen view models

    viewModel<ProfileViewModel> { ProfileViewModelImpl(
        get(), get(), get(), get(), get(), get(), get(), get(named("AvatarsCacheDirectory"))
    ) }

    viewModel<LoginViewModel> { LoginViewModelImpl(get(), get(), get()) }

    viewModel<EditStatisticsViewModel> { EditStatisticsViewModelImpl(get(), get(), get()) }

    viewModel<LinksViewModel> { LinksViewModelImpl(get(), get()) }

    viewModel<AchievementsViewModel> { AchievementsViewModelImpl(get(), get()) }

    viewModel<UserViewModel> { UserViewModelImpl(get()) }

    //endregion

    //region about screen view models

    viewModel<LogsViewModel> { LogsViewModelImpl(get()) }
    viewModel<CreditsViewModel> { CreditsViewModelImpl(get()) }
    viewModel<ChangelogViewModel> { ChangelogViewModelImpl(get()) }

    //endregion

    //region settings screen view models

    viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get(), get(), get(), get(), get(), get()) }
    viewModel<OverlaySelectionViewModel> { OverlaySelectionViewModelImpl(get(), get(), get()) }
    viewModel<LanguageSelectionViewModel> { LanguageSelectionViewModelImpl(get(), get()) }
    viewModel<EditTypePresetsViewModel> { EditTypePresetsViewModelImpl(get(), get(), get(), get()) }
    viewModel<MessageSelectionViewModel> { MessageSelectionViewModelImpl(get()) }
    viewModel<QuestSelectionViewModel> { QuestSelectionViewModelImpl(get(), get(), get(), get(), get(named("CountryBoundariesLazy")), get()) }
    viewModel<ShowQuestFormsViewModel> { ShowQuestFormsViewModelImpl(get(), get()) }

    //endregion
}
