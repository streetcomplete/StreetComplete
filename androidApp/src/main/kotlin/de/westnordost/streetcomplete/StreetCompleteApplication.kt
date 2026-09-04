package de.westnordost.streetcomplete

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.LocaleList
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import de.westnordost.streetcomplete.util.getSelectedLocales
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import java.util.concurrent.TimeUnit

class StreetCompleteApplication : Application() {

    private val crashReportsUncaughtExceptionHandler: CrashReportsUncaughtExceptionHandler by inject()
    private val prefs: Preferences by inject()
    private val cacheTrimmer: CacheTrimmer by inject()
    private val applicationScope: CoroutineScope by inject(named("ApplicationScope"))

    private val settingsListeners = mutableListOf<SettingsListener>()

    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(androidModule, commonModule)
        }

        crashReportsUncaughtExceptionHandler.install()

        updateDefaultLocales()

        koinApplication.koin.get<ApplicationInitializer>().initialize()

        enqueuePeriodicCleanupWork()

        settingsListeners += prefs.onLanguageChanged { updateDefaultLocales() }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE, ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                cacheTrimmer.clearCaches()
            }
            ComponentCallbacks2.TRIM_MEMORY_MODERATE, ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                cacheTrimmer.trimCaches()
            }
        }
    }

    private fun updateDefaultLocales() {
        LocaleList.setDefault(getSelectedLocales(prefs))
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
