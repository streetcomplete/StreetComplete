package de.westnordost.streetcomplete

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.dbModule
import de.westnordost.streetcomplete.data.download.downloadModule
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.editHistoryModule
import de.westnordost.streetcomplete.data.maptiles.maptilesModule
import de.westnordost.streetcomplete.data.meta.metadataModule
import de.westnordost.streetcomplete.data.notifications.notificationsModule
import de.westnordost.streetcomplete.data.osm.created_elements.createdElementsModule
import de.westnordost.streetcomplete.data.osm.edits.elementEditsModule
import de.westnordost.streetcomplete.data.osm.geometry.elementGeometryModule
import de.westnordost.streetcomplete.data.osm.mapdata.mapDataModule
import de.westnordost.streetcomplete.data.osm.osmquests.osmQuestModule
import de.westnordost.streetcomplete.data.osmApiModule
import de.westnordost.streetcomplete.data.osmnotes.edits.noteEditsModule
import de.westnordost.streetcomplete.data.osmnotes.notequests.osmNoteQuestModule
import de.westnordost.streetcomplete.data.osmnotes.notesModule
import de.westnordost.streetcomplete.data.quest.questModule
import de.westnordost.streetcomplete.data.upload.uploadModule
import de.westnordost.streetcomplete.data.user.achievements.achievementsModule
import de.westnordost.streetcomplete.data.user.statistics.statisticsModule
import de.westnordost.streetcomplete.data.user.userModule
import de.westnordost.streetcomplete.data.visiblequests.questPresetsModule
import de.westnordost.streetcomplete.ktx.addedToFront
import de.westnordost.streetcomplete.map.mapModule
import de.westnordost.streetcomplete.measure.arModule
import de.westnordost.streetcomplete.quests.oneway_suspects.data.trafficFlowSegmentsModule
import de.westnordost.streetcomplete.quests.questsModule
import de.westnordost.streetcomplete.settings.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.settings.settingsModule
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.getSelectedLocale
import de.westnordost.streetcomplete.util.getSystemLocales
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit

class StreetCompleteApplication : Application() {

    private val preloader: Preloader by inject()
    private val crashReportExceptionHandler: CrashReportExceptionHandler by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val downloadedTilesDao: DownloadedTilesDao by inject()
    private val prefs: SharedPreferences by inject()
    private val editHistoryController: EditHistoryController by inject()

    private val applicationScope = CoroutineScope(SupervisorJob() + CoroutineName("Application"))

    override fun onCreate() {
        super.onCreate()

        deleteDatabase(ApplicationConstants.OLD_DATABASE_NAME)

        startKoin {
            // Level.ERROR is used as a workaround for this Koin issue:
            // https://github.com/InsertKoinIO/koin/issues/1188 TODO remove when updated to Koin 3.2.0
            androidLogger(Level.ERROR)
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(
                achievementsModule,
                appModule,
                createdElementsModule,
                dbModule,
                downloadModule,
                editHistoryModule,
                elementEditsModule,
                elementGeometryModule,
                mapDataModule,
                mapModule,
                maptilesModule,
                metadataModule,
                noteEditsModule,
                notesModule,
                notificationsModule,
                osmApiModule,
                osmNoteQuestModule,
                osmQuestModule,
                questModule,
                questPresetsModule,
                questsModule,
                settingsModule,
                statisticsModule,
                trafficFlowSegmentsModule,
                uploadModule,
                userModule,
                arModule
            )
        }

        setDefaultLocales()

        crashReportExceptionHandler.install()

        applicationScope.launch {
            preloader.preload()
            editHistoryController.deleteSyncedOlderThan(currentTimeMillis() - ApplicationConstants.MAX_UNDO_HISTORY_AGE)
        }

        enqueuePeriodicCleanupWork()

        setDefaultTheme()

        resurveyIntervalsUpdater.update()

        val lastVersion = prefs.getString(Prefs.LAST_VERSION_DATA, null)
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.edit().putString(Prefs.LAST_VERSION_DATA, BuildConfig.VERSION_NAME).apply()
            if (lastVersion != null) {
                onNewVersion()
            }
        }
    }

    private fun onNewVersion() {
        // on each new version, invalidate quest cache
        downloadedTilesDao.removeAll()
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    private fun setDefaultLocales() {
        val locale = getSelectedLocale(this)
        if (locale != null) {
            setDefaultLocales(getSystemLocales().addedToFront(locale))
        }
    }

    private fun setDefaultTheme() {
        val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
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
}
