package de.westnordost.streetcomplete

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.settings.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import java.lang.System.currentTimeMillis

class StreetCompleteApplication : Application() {

    @Inject lateinit var preloader: Preloader
    @Inject lateinit var crashReportExceptionHandler: CrashReportExceptionHandler
    @Inject lateinit var resurveyIntervalsUpdater: ResurveyIntervalsUpdater
    @Inject lateinit var downloadedTilesDao: DownloadedTilesDao
    @Inject lateinit var prefs: SharedPreferences
    @Inject lateinit var editHistoryController: EditHistoryController

    private val applicationScope = CoroutineScope(SupervisorJob() + CoroutineName("Application"))

    override fun onCreate() {
        super.onCreate()

        deleteDatabase(ApplicationConstants.OLD_DATABASE_NAME)

        Injector.initializeApplicationComponent(this)
        Injector.applicationComponent.inject(this)

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
