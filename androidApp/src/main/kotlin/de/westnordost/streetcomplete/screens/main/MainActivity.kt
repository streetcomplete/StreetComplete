package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import com.russhwolf.settings.SettingsListener
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

/** Android host for the shared Compose Multiplatform main screen. */
class MainActivity :
    BaseActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val preferences: Preferences by inject()

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()
    private val mainBottomSheetViewModel by viewModel<MainBottomSheetViewModel>()

    private val settingsListeners = mutableListOf<SettingsListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) handleIntent(intent)

        val composeView = ComposeView(this)
        setContentView(composeView)
        composeView.setContent {
            AppTheme {
                StreetCompleteApp(
                    mainViewModel = viewModel,
                    editHistoryViewModel = editHistoryViewModel,
                    mainBottomSheetViewModel = mainBottomSheetViewModel,
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
        handleIntent(intent)
    }

    override fun onDestroy() {
        settingsListeners.forEach { it.deactivate() }
        settingsListeners.clear()
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        intent.data?.toString()?.let(viewModel::setUri)
    }

    private fun updateScreenOn() {
        if (preferences.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
