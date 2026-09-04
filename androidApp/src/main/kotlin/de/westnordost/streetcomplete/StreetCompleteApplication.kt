package de.westnordost.streetcomplete

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.CacheTrimmer
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import de.westnordost.streetcomplete.util.getSelectedLocales
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.Locale

class StreetCompleteApplication : Application() {

    private val crashReportsUncaughtExceptionHandler: CrashReportsUncaughtExceptionHandler by inject()
    private val prefs: Preferences by inject()
    private val cacheTrimmer: CacheTrimmer by inject()
    private val applicationInitializer: ApplicationInitializer by inject()

    private val settingsListeners = mutableListOf<SettingsListener>()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@StreetCompleteApplication)
            workManagerFactory()
            modules(androidModule, commonModule)
        }

        crashReportsUncaughtExceptionHandler.install()

        applicationInitializer.initialize()

        updateDefaultLocales()
        updateTheme(prefs.theme)

        settingsListeners += prefs.onLanguageChanged { updateDefaultLocales() }
        settingsListeners += prefs.onThemeChanged { updateTheme(it) }
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

    private fun updateDefaultLocales() {
        val locales = getSelectedLocales(prefs)
        Locale.setDefault(locales.get(0))
        LocaleList.setDefault(getSelectedLocales(prefs))
    }

    private fun updateTheme(theme: Theme) {
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
    }
}

private val Theme.appCompatNightMode: Int get() = when (this) {
    Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
