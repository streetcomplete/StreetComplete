package de.westnordost.streetcomplete

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class StreetCompleteApplication : Application() {

    private val crashReportsUncaughtExceptionHandler: CrashReportsUncaughtExceptionHandler by inject()
    private val cacheTrimmer: CacheTrimmer by inject()
    private val applicationInitializer: ApplicationInitializer by inject()
    private val appLocaleUpdater: AppLocaleUpdater by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(androidModule, commonModule)
        }

        crashReportsUncaughtExceptionHandler.install()

        applicationInitializer.initialize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // the system resets the default locales to the new configuration
        appLocaleUpdater.update()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // very low on memory -> drop caches
                cacheTrimmer.clearCaches()
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // memory needed, but not critical -> trim only
                cacheTrimmer.trimCaches()
            }
        }
    }
}
