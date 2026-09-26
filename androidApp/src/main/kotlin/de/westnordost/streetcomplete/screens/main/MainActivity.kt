package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.PeriodicCleaner
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.AutoSyncer
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.scope.KoinActivityScope
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

/** Android host for the shared main screen and application lifecycle work. */
class MainActivity : BaseActivity(), AndroidScopeComponent {
    override val scope: Scope by activityScope()

    private val prefs: Preferences by inject()
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) handleIntent(intent)

        setContentView(ComposeView(this).apply {
            setContent {
                AppTheme {
                    KoinActivityScope {
                        MainScreen(
                            viewModel = viewModel,
                            onClickSettings = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
                            onClickQuestSettings = { startActivity(SettingsActivity.createLaunchQuestSettingsIntent(this@MainActivity)) },
                            onClickAbout = { startActivity(Intent(this@MainActivity, AboutActivity::class.java)) },
                            onClickProfile = { startActivity(Intent(this@MainActivity, UserActivity::class.java)) },
                            onClickLogin = {
                                startActivity(Intent(this@MainActivity, UserActivity::class.java).apply {
                                    putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true)
                                })
                            },
                        )
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (prefs.keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.toString()?.let { viewModel.setUri(it) }
        }
    }
}
