package de.westnordost.streetcomplete.screens.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.IncomingUriHandler
import de.westnordost.streetcomplete.AppDestination
import de.westnordost.streetcomplete.StreetCompleteApp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.ui.theme.PreferenceAwareAppTheme
import de.westnordost.streetcomplete.util.getSelectedLocale
import de.westnordost.streetcomplete.util.getSystemLocales
import de.westnordost.streetcomplete.util.ktx.addedToFront
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import org.maplibre.compose.location.rememberDefaultLocationProvider
import java.util.Locale

/** Android host for the shared Compose Multiplatform main screen. */
class MainActivity :
    ComponentActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val preferences: Preferences by inject()
    private val incomingUriHandler: IncomingUriHandler by inject()

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()
    private val mainBottomSheetViewModel by viewModel<MainBottomSheetViewModel>()

    private val settingsListeners = mutableListOf<SettingsListener>()
    private var navigationRequest by mutableStateOf<AppDestination?>(null)
    private var selectedLocale: Locale? = null

    override fun attachBaseContext(base: Context) {
        val locale = getSelectedLocale(preferences)
        selectedLocale = locale
        val localizedBase = if (locale == null) {
            base
        } else {
            Locale.setDefault(locale)
            val locales = getSystemLocales().addedToFront(locale)
            LocaleList.setDefault(locales)
            base.createConfigurationContext(Configuration().also { it.setLocales(locales) })
        }
        super.attachBaseContext(localizedBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startDestination = if (intent.action == Intent.ACTION_MANAGE_NETWORK_USAGE) {
            AppDestination.Settings
        } else {
            AppDestination.Main
        }
        if (savedInstanceState == null && intent.action == Intent.ACTION_VIEW) handleIntent(intent)

        setContent {
            val locationProvider = rememberDefaultLocationProvider()
            val mapAppLauncher = rememberMapAppLauncher()
            PreferenceAwareAppTheme {
                StreetCompleteApp(
                    startDestination = startDestination,
                    navigationRequest = navigationRequest,
                    onNavigationRequestHandled = { navigationRequest = null },
                    mainViewModel = viewModel,
                    editHistoryViewModel = editHistoryViewModel,
                    mainBottomSheetViewModel = mainBottomSheetViewModel,
                    locationProvider = locationProvider,
                    mapAppLauncher = mapAppLauncher,
                    onMainShown = ::updateScreenOn,
                )
            }
        }

        settingsListeners += preferences.onLanguageChanged { ActivityCompat.recreate(this) }
    }

    override fun onStart() {
        super.onStart()
        updateScreenOn()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        selectedLocale?.let { locale ->
            Locale.setDefault(locale)
            val locales = getSystemLocales().addedToFront(locale)
            newConfig.setLocales(locales)
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onRestart() {
        super.onRestart()
        if (selectedLocale != getSelectedLocale(preferences)) ActivityCompat.recreate(this)
    }

    override fun onDestroy() {
        settingsListeners.forEach { it.deactivate() }
        settingsListeners.clear()
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()?.let(incomingUriHandler::submit)
            Intent.ACTION_MANAGE_NETWORK_USAGE -> navigationRequest = AppDestination.Settings
        }
    }

    private fun updateScreenOn() {
        if (preferences.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
