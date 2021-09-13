package de.westnordost.streetcomplete

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.settings.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import kotlinx.coroutines.*
import javax.inject.Inject

class StreetCompleteApplication : Application() {

    @Inject lateinit var preloader: Preloader
    @Inject lateinit var cleaner: Cleaner
    @Inject lateinit var crashReportExceptionHandler: CrashReportExceptionHandler
    @Inject lateinit var resurveyIntervalsUpdater: ResurveyIntervalsUpdater
    @Inject lateinit var downloadedTilesDao: DownloadedTilesDao
    @Inject lateinit var prefs: SharedPreferences

    private val applicationScope = CoroutineScope(SupervisorJob() + CoroutineName("Application"))

    override fun onCreate() {
        super.onCreate()

        deleteDatabase(ApplicationConstants.OLD_DATABASE_NAME)

        Injector.initializeApplicationComponent(this)
        Injector.applicationComponent.inject(this)

        crashReportExceptionHandler.install()

        // do these in the background
        applicationScope.launch {
            preloader.preload()
            cleaner.clean()
        }

        val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)

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
}
