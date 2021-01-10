package de.westnordost.streetcomplete

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.settings.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.FutureTask
import javax.inject.Inject

class StreetCompleteApplication : Application(),
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

	@Inject lateinit var countryBoundariesFuture: FutureTask<CountryBoundaries>
	@Inject lateinit var featuresDictionaryFuture: FutureTask<FeatureDictionary>
	@Inject lateinit var crashReportExceptionHandler: CrashReportExceptionHandler
	@Inject lateinit var resurveyIntervalsUpdater: ResurveyIntervalsUpdater
	@Inject lateinit var downloadedTilesDao: DownloadedTilesDao
	@Inject lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        Injector.initializeApplicationComponent(this)
        Injector.applicationComponent.inject(this)

        crashReportExceptionHandler.install()

        preload()

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

    /** Load some things in the background that are needed later  */
    private fun preload() {
        Log.i(PRELOAD_TAG, "Preloading data")

        // country boundaries are necessary latest for when a quest is opened or on a download
        launch(Dispatchers.IO) {
            countryBoundariesFuture.run()
            Log.i(PRELOAD_TAG, "Loaded country boundaries")
        }
        // names dictionary is necessary when displaying an element that has no name or
        // when downloading the place name quest
        launch(Dispatchers.IO) {
            featuresDictionaryFuture.run()
            Log.i(PRELOAD_TAG, "Loaded features dictionary")
        }
    }

    private fun onNewVersion() {
        // on each new version, invalidate quest cache
        downloadedTilesDao.removeAll()
    }

    companion object {
        private const val PRELOAD_TAG = "Preload"
    }
}
