package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
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
import org.koin.core.scope.Scope

/** Android host for the shared main screen and application lifecycle work. */
class MainActivity : BaseActivity(), AndroidScopeComponent {
    override val scope: Scope by activityScope()

    private val autoSyncer: AutoSyncer by inject()
    private val prefs: Preferences by inject()
    private val feedsUpdater: FeedsUpdater by inject()
    private val periodicCleaner: PeriodicCleaner by inject()
    private val uri = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        // Until v64, this activity hosted the map in a fragment. A task saved by an older version
        // can be restored after an update, and restoring a fragment whose class no longer exists
        // crashes. Dropping the fragment entry keeps the rest of the saved state. Remove once
        // updates from versions before v64 are no longer supported.
        savedInstanceState?.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key")
            ?.remove("android:support:fragments")
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) handleIntent(intent)
        lifecycle.addObserver(autoSyncer)
        feedsUpdater.updateAtMostDaily()
        // Only enqueue cleanup when the UI starts, not when AppInitializer runs a background job.
        periodicCleaner.enqueue()

        setContentView(ComposeView(this).apply {
            setContent {
                AppTheme {
                    KoinActivityScope {
                        MainScreen(
                            uri = uri.value,
                            onConsumedUri = { uri.value = null },
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
        if (intent.action == Intent.ACTION_VIEW) uri.value = intent.data?.toString()
    }
}
