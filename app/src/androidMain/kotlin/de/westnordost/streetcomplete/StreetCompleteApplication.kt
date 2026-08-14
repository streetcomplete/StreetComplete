package de.westnordost.streetcomplete

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.deleteRecursively
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.logs.KermitLogger
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class StreetCompleteApplication : Application() {

    private val preloader: Preloader by inject()
    private val databaseLogger: DatabaseLogger by inject()
    private val crashReportsUncaughtExceptionHandler: CrashReportsUncaughtExceptionHandler by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val downloadedTilesController: DownloadedTilesController by inject()
    private val prefs: Preferences by inject()
    private val editHistoryController: EditHistoryController by inject()
    private val userLoginController: UserLoginController by inject()
    private val cacheTrimmer: CacheTrimmer by inject()
    private val feedsUpdater: FeedsUpdater by inject()
    private val fileSystem: FileSystem by inject()

    private val applicationScope = CoroutineScope(SupervisorJob() + CoroutineName("Application"))

    private val settingsListeners = mutableListOf<SettingsListener>()

    override fun onCreate() {
        super.onCreate()

        deleteDatabase(ApplicationConstants.OLD_DATABASE_NAME)

        startKoin {
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(
                androidModule,
                androidModule2,
                commonModule,
            )
        }

        setLoggerInstances()

        // Force logout users who are logged in with OAuth 1.0a, they need to re-authenticate with OAuth 2
        if (prefs.hasOAuth1AccessToken) {
            userLoginController.logOut()
        }

        updateDefaultLocales()

        crashReportsUncaughtExceptionHandler.install()

        applicationScope.launch {
            preloader.preload()
            editHistoryController.deleteSyncedOlderThan(nowAsEpochMilliseconds() - ApplicationConstants.MAX_UNDO_HISTORY_AGE)
        }

        feedsUpdater.updateNow()

        enqueuePeriodicCleanupWork()

        updateTheme(prefs.theme)

        resurveyIntervalsUpdater.update()

        val lastVersion = prefs.lastDataVersion
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.lastDataVersion = BuildConfig.VERSION_NAME
            if (lastVersion != null) {
                onNewVersion()
            }
        }
        clearTangramCache()

        settingsListeners += prefs.onLanguageChanged { updateDefaultLocales() }
        settingsListeners += prefs.onThemeChanged { updateTheme(it) }
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
        LocaleList.setDefault(getSelectedLocales(prefs))
    }

    private fun updateTheme(theme: Theme) {
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
    }

    private fun setLoggerInstances() {
        Log.instances.add(KermitLogger())
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

    private fun clearTangramCache() {
        if (prefs.clearedTangramCache) return
        val externalCache = externalCacheDir ?: return
        val tileCache = Path(externalCache.path, "tile_cache")
        if (!fileSystem.exists(tileCache)) return
        applicationScope.launch(Dispatchers.IO) {
            fileSystem.deleteRecursively(tileCache, mustExist = false)
            prefs.clearedTangramCache = true
        }
    }
}

private val Theme.appCompatNightMode: Int get() = when (this) {
    Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
