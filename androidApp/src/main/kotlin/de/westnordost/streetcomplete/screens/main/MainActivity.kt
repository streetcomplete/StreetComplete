package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.IncomingUriHandler
import de.westnordost.streetcomplete.AppDestination
import de.westnordost.streetcomplete.StreetCompleteApp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import org.maplibre.compose.location.rememberDefaultLocationProvider

/** Android host for the shared Compose Multiplatform main screen. */
class MainActivity :
    BaseActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val preferences: Preferences by inject()
    private val incomingUriHandler: IncomingUriHandler by inject()

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()
    private val mainBottomSheetViewModel by viewModel<MainBottomSheetViewModel>()

    private val settingsListeners = mutableListOf<SettingsListener>()
    private var navigationRequest by mutableStateOf<AppDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startDestination = if (intent.action == Intent.ACTION_MANAGE_NETWORK_USAGE) {
            AppDestination.Settings
        } else {
            AppDestination.Main
        }
        if (savedInstanceState == null && intent.action == Intent.ACTION_VIEW) handleIntent(intent)

        val composeView = ComposeView(this)
        setContentView(composeView)
        composeView.setContent {
            val locationProvider = rememberDefaultLocationProvider()
            AppTheme {
                StreetCompleteApp(
                    startDestination = startDestination,
                    navigationRequest = navigationRequest,
                    onNavigationRequestHandled = { navigationRequest = null },
                    mainViewModel = viewModel,
                    editHistoryViewModel = editHistoryViewModel,
                    mainBottomSheetViewModel = mainBottomSheetViewModel,
                    locationProvider = locationProvider,
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
