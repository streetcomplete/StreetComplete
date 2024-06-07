package de.westnordost.streetcomplete

import android.app.Application
import android.content.ComponentCallbacks2
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.allEditTypesModule
import de.westnordost.streetcomplete.data.dbModule
import de.westnordost.streetcomplete.data.download.downloadModule
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.editHistoryModule
import de.westnordost.streetcomplete.data.logs.logsModule
import de.westnordost.streetcomplete.data.maptiles.maptilesModule
import de.westnordost.streetcomplete.data.messages.messagesModule
import de.westnordost.streetcomplete.data.meta.metadataModule
import de.westnordost.streetcomplete.data.osm.created_elements.createdElementsModule
import de.westnordost.streetcomplete.data.osm.edits.elementEditsModule
import de.westnordost.streetcomplete.data.osm.geometry.elementGeometryModule
import de.westnordost.streetcomplete.data.osm.mapdata.mapDataModule
import de.westnordost.streetcomplete.data.osm.osmquests.osmQuestModule
import de.westnordost.streetcomplete.data.osmApiModule
import de.westnordost.streetcomplete.data.osmnotes.edits.noteEditsModule
import de.westnordost.streetcomplete.data.osmnotes.notequests.osmNoteQuestModule
import de.westnordost.streetcomplete.data.osmnotes.notesModule
import de.westnordost.streetcomplete.data.overlays.overlayModule
import de.westnordost.streetcomplete.data.platform.platformModule
import de.westnordost.streetcomplete.data.quest.questModule
import de.westnordost.streetcomplete.data.upload.uploadModule
import de.westnordost.streetcomplete.data.urlconfig.urlConfigModule
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.achievementsModule
import de.westnordost.streetcomplete.data.user.statistics.statisticsModule
import de.westnordost.streetcomplete.data.user.userModule
import de.westnordost.streetcomplete.data.visiblequests.questPresetsModule
import de.westnordost.streetcomplete.overlays.overlaysModule
import de.westnordost.streetcomplete.quests.oneway_suspects.data.trafficFlowSegmentsModule
import de.westnordost.streetcomplete.quests.questsModule
import de.westnordost.streetcomplete.screens.about.aboutScreenModule
import de.westnordost.streetcomplete.screens.main.mainModule
import de.westnordost.streetcomplete.screens.main.map.mapModule
import de.westnordost.streetcomplete.screens.measure.arModule
import de.westnordost.streetcomplete.screens.settings.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.screens.settings.settingsModule
import de.westnordost.streetcomplete.screens.user.userScreenModule
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.getDefaultTheme
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.AndroidLogger
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.prefs.preferencesModule
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class StreetCompleteApplication : Application() {

    private val preloader: Preloader by inject()
    private val databaseLogger: DatabaseLogger by inject()
    private val crashReportExceptionHandler: CrashReportExceptionHandler by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val downloadedTilesController: DownloadedTilesController by inject()
    private val prefs: ObservableSettings by inject()
    private val editHistoryController: EditHistoryController by inject()
    private val userLoginController: UserLoginController by inject()
    private val cacheTrimmer: CacheTrimmer by inject()
    private val userUpdater: UserUpdater by inject()

    private val applicationScope = CoroutineScope(SupervisorJob() + CoroutineName("Application"))

    private val settingsListeners = mutableListOf<SettingsListener>()

    override fun onCreate() {
        super.onCreate()

        deleteDatabase(ApplicationConstants.OLD_DATABASE_NAME)

        startKoin {
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(
                achievementsModule,
                appModule,
                aboutScreenModule,
                userScreenModule,
                createdElementsModule,
                dbModule,
                logsModule,
                downloadModule,
                editHistoryModule,
                elementEditsModule,
                elementGeometryModule,
                mapDataModule,
                mapModule,
                mainModule,
                maptilesModule,
                metadataModule,
                noteEditsModule,
                notesModule,
                messagesModule,
                osmApiModule,
                osmNoteQuestModule,
                osmQuestModule,
                preferencesModule,
                questModule,
                questPresetsModule,
                allEditTypesModule,
                questsModule,
                settingsModule,
                statisticsModule,
                trafficFlowSegmentsModule,
                uploadModule,
                userModule,
                arModule,
                overlaysModule,
                overlayModule,
                urlConfigModule,
                platformModule
            )
        }

        setLoggerInstances()

        // Force logout users who are logged in with OAuth 1.0a, they need to re-authenticate with OAuth 2
        if (prefs.getStringOrNull(Prefs.OAUTH1_ACCESS_TOKEN) != null) {
            userLoginController.logOut()
        }

        updateDefaultLocales()

        crashReportExceptionHandler.install()

        applicationScope.launch {
            preloader.preload()
            editHistoryController.deleteSyncedOlderThan(nowAsEpochMilliseconds() - ApplicationConstants.MAX_UNDO_HISTORY_AGE)
        }

        if (isConnected) userUpdater.update()

        enqueuePeriodicCleanupWork()

        updateDefaultTheme()

        resurveyIntervalsUpdater.update()

        val lastVersion = prefs.getStringOrNull(Prefs.LAST_VERSION_DATA)
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.putString(Prefs.LAST_VERSION_DATA, BuildConfig.VERSION_NAME)
            if (lastVersion != null) {
                onNewVersion()
            }
        }

        settingsListeners += prefs.addStringOrNullListener(Prefs.LANGUAGE_SELECT) {
            updateDefaultLocales()
        }
        settingsListeners += prefs.addStringOrNullListener(Prefs.THEME_SELECT) {
            updateDefaultTheme()
        }
    }

    private fun onNewVersion() {
        // on each new version, invalidate quest cache
        downloadedTilesController.invalidateAll()
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE, ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // very low on memory -> drop caches
                cacheTrimmer.clearCaches()
            }
            ComponentCallbacks2.TRIM_MEMORY_MODERATE, ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // memory needed, but not critical -> trim only
                cacheTrimmer.trimCaches()
            }
        }
    }

    private fun updateDefaultLocales() {
        setDefaultLocales(getSelectedLocales(prefs))
    }

    private fun updateDefaultTheme() {
        val theme = Prefs.Theme.valueOf(prefs.getStringOrNull(Prefs.THEME_SELECT) ?: getDefaultTheme())
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
    }

    private fun setLoggerInstances() {
        Log.instances.add(AndroidLogger())
        Log.instances.add(databaseLogger)
    }

    private fun enqueuePeriodicCleanupWork() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "Cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequest.Builder(
                CleanerWorker::class.java,
                1, TimeUnit.DAYS,
                1, TimeUnit.DAYS,
            ).setInitialDelay(1, TimeUnit.HOURS).build()
        )
    }

    private val isConnected: Boolean
        get() = getSystemService<ConnectivityManager>()?.activeNetworkInfo?.isConnected == true
}
